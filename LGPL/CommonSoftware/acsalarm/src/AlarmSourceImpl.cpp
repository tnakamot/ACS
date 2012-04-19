/*
 *    ALMA - Atacama Large Millimiter Array
 *    (c) European Southern Observatory, 2011
 *    Copyright by ESO (in the framework of the ALMA collaboration),
 *    All rights reserved
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA 02111-1307  USA
 */

#include <sstream>

#include "AlarmSourceImpl.h"
#include <faultStateConstants.h>

#include <ace/Guard_T.h>

using namespace acsalarm;

AlarmSourceImpl::AlarmSourceImpl():
	ACS::Thread("AlarmsourceImpl", 10000000),
	m_disabled(false),
	m_queuing(false),
	m_updateMap(0),
	m_alarmSource_ap(NULL)
{
}

AlarmSourceImpl::~AlarmSourceImpl() {
	m_alarmSource_ap.release();
}

void AlarmSourceImpl::raiseAlarm(
		std::string faultFamily,
		std::string faultMember,
		int faultCode)
{
	Properties properties;
	setAlarm(faultFamily,faultMember,faultCode,properties,true);
}

/**
 * @see AlarmSource
 */
void AlarmSourceImpl::raiseAlarm(
		std::string faultFamily,
		std::string faultMember,
		int faultCode,
		Properties properties)
{
	setAlarm(faultFamily,faultMember,faultCode,properties,true);
}

/**
 * @see AlarmSource
 */
void AlarmSourceImpl::clearAlarm(
		std::string faultFamily,
		std::string faultMember,
		int faultCode)
{
	Properties properties;
	setAlarm(faultFamily,faultMember,faultCode,properties,false);
}

/**
 * @see AlarmSource
 */
void AlarmSourceImpl::setAlarm(
		std::string faultFamily,
		std::string faultMember,
		int faultCode,
		Properties alarmProps,
		bool active)
{
	ACE_Guard<ACE_Recursive_Thread_Mutex> guard(m_mutex);
	if (m_disabled) {
		return;
	}
	std::string alarmID=buildAlarmID(faultFamily,faultMember,faultCode);
	if (m_queuing) {
		AlarmToQueue* alarm = new AlarmToQueue(faultFamily,faultMember,faultCode,alarmProps,active);
		m_queue[alarmID]=alarm;
		return;
	}
	// Check if the alarm must be sent to the AS
	bool toSend;
	if (active) {
		toSend=m_alarms.raise(alarmID);
	} else {
		toSend=m_alarms.clear(alarmID);
	}
	if (toSend) {
		internalAlarmSender(faultFamily,faultMember,faultCode,alarmProps,active);
		if (active)
		{
			m_activatedAlarms.insert(alarmID);
		}
		else
		{
			m_activatedAlarms.erase(alarmID);
		}
	}
}

/**
 * @see AlarmSource
 */
void AlarmSourceImpl::setAlarm(
		std::string faultFamily,
		std::string faultMember,
		int faultCode,
		bool active)
{
	Properties properties;
	setAlarm(faultFamily,faultMember,faultCode,properties,active);
}

/**
 * @see AlarmSource
 */
void AlarmSourceImpl::terminateAllAlarms()
{
	ACE_Guard<ACE_Recursive_Thread_Mutex> guard(m_mutex);
	if (!m_activatedAlarms.empty()) {
		std::set<std::string>::iterator it;
		Properties props;
		for (it=m_activatedAlarms.begin(); it!=m_activatedAlarms.end(); it++) {
			std::string id=(*it);
			int first=id.find(":");
			int last=id.rfind(":");
			std::string ffStr=id.substr(0,first);
			std::string fmStr=id.substr(first+1,last-first-1);
			std::string fcStr=id.substr(last+1);
			std::istringstream iss(fcStr);
			int fcInt;
			iss>>fcInt;
			internalAlarmSender(ffStr,fmStr,fcInt,props,false);
		}
		m_activatedAlarms.clear();
	}
}

/**
 * @see AlarmSource
 */
void AlarmSourceImpl::queueAlarms(ACS::TimeInterval time)
{
	ACE_Guard<ACE_Recursive_Thread_Mutex> guard(m_mutex);
	m_queuing=true;
	// start the thread
	setSleepTime(time);
	resume();
}

/**
 * @see AlarmSource
 */
void AlarmSourceImpl::queueAlarms()
{
	ACE_Guard<ACE_Recursive_Thread_Mutex> guard(m_mutex);
	m_queuing=true;
}

/**
 * @see AlarmSource
 */
void AlarmSourceImpl::flushAlarms()
{
	ACE_Guard<ACE_Recursive_Thread_Mutex> guard(m_mutex);
	// The queuing must be disabled immediately otherwise the call to setAlarm
	// will add the alarm in the queue again
	m_queuing=false;
	if (m_queue.empty()) {
		return;
	}
	std::map<std::string, AlarmToQueue*>::iterator it;
	for (it=m_queue.begin(); it!=m_queue.end(); it++) {
		AlarmToQueue* alarm = (*it).second;
		setAlarm(alarm->getFF(),alarm->getFM(),alarm->getFC(),alarm->getAlarmProps(),alarm->isActive());
		delete alarm;
	}
	m_queue.clear();
	suspend(); // Suspend the thread
}

/**
 * @see AlarmSource
 */
void AlarmSourceImpl::disableAlarms()
{
	ACE_Guard<ACE_Recursive_Thread_Mutex> guard(m_mutex);
	m_disabled=true;
}

/**
 * @see AlarmSource
 */
void AlarmSourceImpl::enableAlarms()
{
	ACE_Guard<ACE_Recursive_Thread_Mutex> guard(m_mutex);
	m_disabled=false;
}

/**
 * @see AlarmSource
 */
void AlarmSourceImpl::start()
{
	auto_ptr<acsalarm::AlarmSystemInterface> temp(ACSAlarmSystemInterfaceFactory::createSource());
	m_alarmSource_ap= temp;
	m_alarms.start();
}

/**
 * @see AlarmSource
 */
void AlarmSourceImpl::tearDown()
{
	// Stop the thread
	terminate();
	m_alarms.shutdown();
	m_activatedAlarms.clear();
}

std::string AlarmSourceImpl::buildAlarmID(std::string faultFamily, std::string faultMember, int faultCode)
{
	std::ostringstream oss;
	oss << faultFamily << ':';
	oss << faultMember << ':';
	oss << faultCode;
	return oss.str();

}

void AlarmSourceImpl::internalAlarmSender(
				std::string faultFamily,
				std::string faultMember,
				int faultCode,
				Properties alarmProps,
				bool active)
{
	 // create the FaultState
	auto_ptr<acsalarm::FaultState> fltstate = ACSAlarmSystemInterfaceFactory::createFaultState(faultFamily, faultMember, faultCode);

	// set the fault state's descriptor
	std::string stateString;
	if (active)
	{
			stateString = faultState::ACTIVE_STRING;
	} else {
			stateString = faultState::TERMINATE_STRING;
	}
	fltstate->setDescriptor(stateString);

	// create a Timestamp and use it to configure the FaultState
	Timestamp * tstampPtr = new Timestamp();
	auto_ptr<Timestamp> tstampAutoPtr(tstampPtr);
	fltstate->setUserTimestamp(tstampAutoPtr);

	// Set the Properties object and configure it, then assign to the FaultState
	if (alarmProps.getSize()>0) {
		auto_ptr<Properties> props(new Properties(alarmProps));
		fltstate->setUserProperties(props);
	}

	// Send the alarm
	m_alarmSource_ap->push(*fltstate);
}

void AlarmSourceImpl::runLoop()
{
	ACE_Guard<ACE_Recursive_Thread_Mutex> guard(m_mutex);
	flushAlarms();

	// Update the AlarmsMap internal data structures every
	// 10 iterations i.e. once per second.
	m_updateMap= (m_updateMap+1)%10;
	if (m_updateMap==0) {
		m_alarms.updateInternalDataStructs();
	}
}
