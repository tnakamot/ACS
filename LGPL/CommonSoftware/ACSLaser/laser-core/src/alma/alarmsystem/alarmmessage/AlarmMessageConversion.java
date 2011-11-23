/*
 *    ALMA - Atacama Large Millimiter Array
 *    (c) European Southern Observatory, 2011
 *    Copyright by ESO (in the framework of the ALMA collaboration),
 *                 and Cosylab
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
package alma.alarmsystem.alarmmessage;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Iterator;
import java.io.StringReader;
import java.util.Enumeration;

import alma.alarmsystem.alarmmessage.generated.*;

import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.MarshalException;

import cern.laser.business.data.Location;
import cern.laser.business.data.Building;
import cern.laser.business.data.ResponsiblePerson;
import cern.laser.business.data.Source;
import cern.laser.business.data.SourceStatus;
import cern.laser.business.data.Triplet;
import cern.laser.business.data.StatusImpl;
import cern.laser.business.data.AlarmImpl;
import cern.laser.business.data.CategoryImpl;

/**
 * This class manages the conversion between the ACSAlarmMessage
 * class generated by castor and the AlarmImpl classes in laser-core and
 * laser-source.
 * It contains other utility methods related to the generated code as well.
 * 
 * 
 * @author acaproni
 *
 */
public class AlarmMessageConversion {
	
	/**
	 * Take a (laser-core) alarm and returns its XML representation
	 * generated by castor
	 * 
	 * @param coreAlarm An AlarmImpl in the laser-core
	 * @return The XML representation of the alarm
	 */
	public static synchronized String getXML(AlarmImpl coreAlarm) 
	throws ValidationException, MarshalException {
		if (coreAlarm==null) {
			throw new IllegalArgumentException();
		}
		// Build an ACSAlarmMessage object
		ACSAlarmMessage acsAlarm = new ACSAlarmMessage();
		
		// Fill the simple fields of the ACSAlarmMessage
		acsAlarm.setAlarmId(coreAlarm.getAlarmId());
		acsAlarm.setSystemName(coreAlarm.getSystemName());
		acsAlarm.setIdent(coreAlarm.getIdentifier());
		acsAlarm.setProblemDescription(coreAlarm.getProblemDescription());
		if (coreAlarm.getPriority()!=null) {
			acsAlarm.setPriority(coreAlarm.getPriority().intValue());
		}
		acsAlarm.setCause(coreAlarm.getCause());
		acsAlarm.setAction(coreAlarm.getAction());
		acsAlarm.setConsequence(coreAlarm.getConsequence());
		acsAlarm.setPiquetGSM(coreAlarm.getPiquetGSM());
		acsAlarm.setPiquetEmail(coreAlarm.getPiquetEmail());
		acsAlarm.setHelpURLString(coreAlarm.getHelpURLString());
		if (coreAlarm.getInstant()!=null) {
			acsAlarm.setInstant(coreAlarm.getInstant().booleanValue());
		}
		if (coreAlarm.getMultiplicityThreshold()!=null) {
			acsAlarm.setMultiplicityThreshold(coreAlarm.getMultiplicityThreshold().intValue());
		}
		
		// Fill the other fields
		acsAlarm.setTriplet(createACSTriplet(coreAlarm.getTriplet()));
		acsAlarm.setSource(createACSSource(coreAlarm.getSource()));
		acsAlarm.setResponsiblePerson(createACSResponsiblePerson(coreAlarm.getResponsiblePerson()));
		
		// NodeParentIds
		if (coreAlarm.getNodeParentIds()!=null) {
			Iterator iter = coreAlarm.getNodeParentIds().iterator();
			acsAlarm.clearNodeParentIds();
			while (iter.hasNext()) {
				acsAlarm.addNodeParentIds(iter.next().toString());
			}
		}
		
		// NodeChildrenIds
		if (coreAlarm.getNodeChildrenIds()!=null) {
			Iterator iter = coreAlarm.getNodeChildrenIds().iterator();
			acsAlarm.clearNodeChildrenIds();
			while (iter.hasNext()) {
				acsAlarm.addNodeChildrenIds(iter.next().toString());
			}
		}
		
		// MultiplicityChildrenIds
		if (coreAlarm.getMultiplicityChildrenIds()!=null) {
			Iterator iter = coreAlarm.getMultiplicityChildrenIds().iterator();
			acsAlarm.clearMultiplicityChildrenIds();
			while (iter.hasNext()) {
				acsAlarm.addMultiplicityChildrenIds(iter.next().toString());
			}
		}
		
		// MultiplicityParentIds
		if (coreAlarm.getMultiplicityParentIds()!=null) {
			Iterator iter = coreAlarm.getMultiplicityParentIds().iterator();
			acsAlarm.clearMultiplicityParentIds();
			while (iter.hasNext()) {
				acsAlarm.addMultiplicityParentIds(iter.next().toString());
			}
		}
		
		acsAlarm.setLocation(createACSLocation(coreAlarm.getLocation()));
		acsAlarm.setStatus(createACSStatus((cern.laser.business.data.StatusImpl)coreAlarm.getStatus()));
		
		////////////////////////////////////////////////
		if (coreAlarm.getCategories()!=null && coreAlarm.getCategories().size()>0) {
			ACSCategory[] categories = new ACSCategory[coreAlarm.getCategories().size()];
			int pos=0;
			Iterator iter = coreAlarm.getCategories().iterator();
			while (iter.hasNext()) {
				categories[pos++]=createACSCategory((cern.laser.business.data.CategoryImpl)iter.next());
			}
			acsAlarm.setCategories(categories);
		}
		
		acsAlarm.setNodeParent(coreAlarm.hasNodeChildren());
		acsAlarm.setMultiplicityParent(coreAlarm.hasMultiplicityChildren());
		acsAlarm.setNodeChild(coreAlarm.hasNodeParents());
		acsAlarm.setMultiplicityChild(coreAlarm.hasMultiplicityParents());
		// Generate the XML
		StringWriter strWriter = new StringWriter();
		acsAlarm.marshal(strWriter);
		return strWriter.toString();
	}
	
	/**
	 * Generate an ACSSource object from a (laser-core) Source
	 * 
	 * @param coreSource The laser-core Source
	 * @return The ACSource
	 */
	private static ACSSource createACSSource(Source coreSource) {
		if (coreSource==null) {
			return null;
		}
		ACSSource source = new ACSSource();
		source.setSourceId(coreSource.getSourceId());
		source.setDescription(coreSource.getDescription());
		if (coreSource.getConnectionTimeout()!=null) {
			source.setConnectionTimeout(coreSource.getConnectionTimeout().intValue());
		}
		source.setSurveillanceAlarmId(coreSource.getSurveillanceAlarmId());
		source.setHostName(coreSource.getHostName());
		source.setSourceStatus(createACSSourceStatus(coreSource.getStatus()));
		source.setResponsiblePerson(createACSResponsiblePerson(coreSource.getResponsiblePerson()));
		
		source.setAlarmIds(setToArrayOfString(coreSource.getAlarmIds()));
		
		return source;
	}
	
	/**
	 * Generate an ACSTimestamp from a java.sql.Timestamp
	 * 
	 * @param sqlStamp The java.sql.Timestamp
	 * @return The ACSTimestamp
	 */
	private static ACSTimestamp createACSTimestamp(Timestamp sqlStamp) {
		if (sqlStamp==null) {
			return null;
		}
		ACSTimestamp stamp = new ACSTimestamp();
		stamp.setDate(sqlStamp.getTime());
		stamp.setNanos(sqlStamp.getNanos());
		return stamp;
	}
	
	/**
	 * Generate an ACSSourceStatus from a cern.laser.business.data.SourceStatus
	 * 
	 * @param coreSourceStatus The cern.laser.business.data.SourceStatus
	 * @return The ACSSourceStatus
	 */
	private static ACSSourceStatus createACSSourceStatus(SourceStatus coreSourceStatus) {
		if (coreSourceStatus==null) {
			return null;
		}
		ACSSourceStatus sourceStatus = new ACSSourceStatus();
		sourceStatus.setStatusId(coreSourceStatus.getStatusId());
		if (coreSourceStatus.getConnected()!=null) {
			sourceStatus.setConnected(coreSourceStatus.getConnected().booleanValue());
		}
		if (coreSourceStatus.getEnabled()!=null) {
			sourceStatus.setEnabled(coreSourceStatus.getEnabled().booleanValue());
		}
		sourceStatus.setLastContact(createACSTimestamp(coreSourceStatus.getLastContact()));
		return sourceStatus;
	}
	
	/**
	 * Generate an ACSResponsiblePerson from a cern.laser.business.data.ResponsiblePerson
	 * 
	 * @param coreSourceStatus The cern.laser.business.data.ResponsiblePerson
	 * @return The ACSResponsiblePerson
	 */
	private static ACSResponsiblePerson createACSResponsiblePerson(ResponsiblePerson coreResponsible) {
		if (coreResponsible==null) {
			return null;
		}
		ACSResponsiblePerson responsible = new ACSResponsiblePerson();
		if (coreResponsible.getResponsibleId()!=null) {
			responsible.setResponsibleId(coreResponsible.getResponsibleId().intValue());
		}
		responsible.setFirstName(coreResponsible.getFirstName());
		responsible.setFamilyName(coreResponsible.getFamilyName());
		responsible.setEMail(coreResponsible.getEMail());
		responsible.setGsmNumber(coreResponsible.getGsmNumber());
		responsible.setPhoneNumber(coreResponsible.getPhoneNumber());
		return responsible;
		
	}
	
	/**
	 * Generate an ACSTriplet from a cern.laser.business.data.Triple
	 * 
	 * @param coreSourceStatus The cern.laser.business.data.Triplet
	 * @return The ACSTriplet
	 */
	private static ACSTriplet createACSTriplet(Triplet coreTriplet) {
		if (coreTriplet==null) {
			return null;
		}
		ACSTriplet triplet = new ACSTriplet();
		if (coreTriplet.getFaultCode()!=null) {
			triplet.setFaultCode(coreTriplet.getFaultCode().intValue());
		}
		triplet.setFaultFamily(coreTriplet.getFaultFamily());
		triplet.setFaultMember(coreTriplet.getFaultMember());
		return triplet;
	}
	
	/**
	 * Generate an ACSBuilding from a cern.laser.business.data.Building
	 * 
	 * @param coreSourceStatus The cern.laser.business.data.Building
	 * @return The ACSBuilding
	 */
	private static ACSBuilding createACSBuilding(Building coreBuilding) {
		if (coreBuilding==null) {
			return null;
		}
		ACSBuilding building = new ACSBuilding();
		building.setBuildingNumber(coreBuilding.getBuildingNumber());
		building.setSite(coreBuilding.getSite());
		if (coreBuilding.getZone()!=null) {
			building.setZone(coreBuilding.getZone().intValue());
		}
		building.setMap(coreBuilding.getMap());
		return building;
	}
	
	/**
	 * Generate an ACSLocation from a cern.laser.business.data.Location
	 * 
	 * @param coreSourceStatus The cern.laser.business.data.Location
	 * @return The ACSLocation
	 */
	private static ACSLocation createACSLocation(Location coreLocation) {
		if (coreLocation==null) {
			return null;
		}
		ACSLocation location = new ACSLocation();
		location.setLocationId(coreLocation.getLocationId());
		location.setFloor(coreLocation.getFloor());
		location.setRoom(coreLocation.getRoom());
		location.setMnemonic(coreLocation.getMnemonic());
		location.setPosition(coreLocation.getPosition());
		location.setBuilding(createACSBuilding(coreLocation.getBuilding()));
		return location;
	}
	
	/**
	 * Generate an ACSStatus from a cern.laser.business.data.StatusImpl
	 * 
	 * @param coreSourceStatus The cern.laser.business.data.Location
	 * @return The ACSStatus
	 */
	private static ACSStatus createACSStatus(StatusImpl coreStatus) {
		if (coreStatus==null) {
			return null;
		}
		ACSStatus status = new ACSStatus();
		status.setStatusId(coreStatus.getStatusId());
		if (coreStatus.getActive()!=null) {
			status.setActive(coreStatus.getActive().booleanValue());
		}
		if (coreStatus.getMasked()!=null) {
			status.setMasked(coreStatus.getMasked().booleanValue());
		}
		if (coreStatus.getReduced()!=null) {
			status.setReduced(coreStatus.getReduced().booleanValue());
		}
		if (coreStatus.getTerminatedByBackup()!=null) {
			status.setTerminatedByBackup(coreStatus.getTerminatedByBackup().booleanValue());
		}
		if (coreStatus.getActivatedByBackup()!=null) {
			status.setActivatedByBackup(coreStatus.getActivatedByBackup().booleanValue());
		}
		status.setSourceTimestamp(createACSTimestamp(coreStatus.getSourceTimestamp()));
		status.setUserTimestamp(createACSTimestamp(coreStatus.getUserTimestamp()));
		status.setSystemTimestamp(createACSTimestamp(coreStatus.getSystemTimestamp()));
		status.setSourceHostname(coreStatus.getSourceHostname());
		
		if (coreStatus.getProperties()!=null) {
			Properties coreProps = coreStatus.getProperties();
			for (Enumeration coreKeys = coreProps.propertyNames(); coreKeys.hasMoreElements() ;) {
		         String key = (String)coreKeys.nextElement();
		         String val = (String)coreProps.getProperty(key);
		         ACSProperty oneProp= new ACSProperty();
		         oneProp.setKey(key);
		         oneProp.setValue(val);
		         status.addPersistentUserProperties(oneProp);
			}
     }
		// globalAcknowledged is not used in StatusImpl so I skip the field
		return status;
	}
	
	private static ACSCategory createACSCategory(CategoryImpl coreCategory) {
		if (coreCategory==null) {
			return null;
		}
		ACSCategory category = new ACSCategory();
		category.setName(coreCategory.getName());
		category.setDescription(coreCategory.getDescription());
		if (coreCategory.getCategoryId()!=null) {
			category.setCategoryId(coreCategory.getCategoryId().intValue());
		}
		if (coreCategory.getParentId()!=null) {
			category.setParentId(coreCategory.getParentId().intValue());
		}
		category.setChildrenIds(setToArrayOfString(coreCategory.getChildrenIds()));
		category.setAlarmIds(setToArrayOfString(coreCategory.getAlarmIds()));
		category.setPath(coreCategory.getPath());
		category.setIsLeaf(coreCategory.isLeaf());
		return category;
	}
	
	/**
	 * Converts a set of String to an array of String
	 * 
	 * @param set The set to convert
	 * @return The set as array of String
	 */
	private static String[] setToArrayOfString(Set set) {
		if (set==null) {
			return null;
		}
		Iterator iter = set.iterator();
		String[] res = new String[set.size()];
		int pos=0;
		Object obj=null;
		while (iter.hasNext()) {
			try {
				obj = iter.next();
				res[pos++]=obj.toString();
			} catch (ClassCastException cce) {
				System.err.println("*** Calls cast exception: "+cce.getMessage());
				System.err.println("*** Original class is "+obj.getClass().getName());
				cce.printStackTrace();
			}
		}
		return res;
	}
	
	public static synchronized AlarmImpl getAlarm(String xml) 
	throws ValidationException, MarshalException  {
		if (xml==null) {
			throw new IllegalArgumentException();
		}
		StringReader strReader = new StringReader(xml);
		ACSAlarmMessage alarm = ACSAlarmMessage.unmarshalACSAlarmMessage(strReader);
		
		// Primitive field
		String alarmId = alarm.getAlarmId();
		String systemName = alarm.getSystemName();
		String identifier = alarm.getIdent();
		String problemDescription = alarm.getProblemDescription();
		Integer priority = new Integer(alarm.getPriority());
		String cause = alarm.getCause();
		String action = alarm.getAction();
		String consequence = alarm.getConsequence();
		String piquetGSM = alarm.getPiquetGSM();
		String piquetEmail = alarm.getPiquetEmail();
		String helpURLString = alarm.getHelpURLString();
		Boolean instant = new Boolean(alarm.getInstant());
		boolean nodeParent = alarm.getNodeParent();
		boolean multiplicityParent = alarm.getMultiplicityParent();
		boolean nodeChild = alarm.getNodeChild();
		boolean multiplicityChild = alarm.getMultiplicityChild();
		
		// The Triplet
		Triplet triplet = new Triplet(
				alarm.getTriplet().getFaultFamily(),
				alarm.getTriplet().getFaultMember(),
				new Integer(alarm.getTriplet().getFaultCode()));
		
		// The Building
		Building building;
		if (alarm.getLocation()!=null) {
			if (alarm.getLocation().getBuilding()!=null){
				building = new Building (
					alarm.getLocation().getBuilding().getBuildingNumber(),
					alarm.getLocation().getBuilding().getSite(),
					new Integer(alarm.getLocation().getBuilding().getZone()),
					alarm.getLocation().getBuilding().getMap());
			} else {
				building=new Building("N/A",",N/A",0,"N/A");
			}
		} else {
			building=new Building("N/A",",N/A",0,"N/A");
		}
		
		// The Location
		Location location;
		if (alarm.getLocation()!=null) {
			location = new Location (
				alarm.getLocation().getLocationId(),
				alarm.getLocation().getFloor(),
				alarm.getLocation().getMnemonic(),
				alarm.getLocation().getPosition(),
				alarm.getLocation().getRoom());
		} else {
			location=new Location("N/A","N/A","N/A","N/A","N/A");
		}
		location.setBuilding(building);
		
		// The ResponsiblePerson
		ResponsiblePerson responsiblePerson =null;
		if (alarm.getResponsiblePerson()!=null) {
				responsiblePerson = new ResponsiblePerson (
				new Integer (alarm.getResponsiblePerson().getResponsibleId()),
				alarm.getResponsiblePerson().getFirstName(),
				alarm.getResponsiblePerson().getFamilyName(),
				alarm.getResponsiblePerson().getEMail(),
				alarm.getResponsiblePerson().getGsmNumber(),
				alarm.getResponsiblePerson().getPhoneNumber());
		}
		
		// The Source
		Source source = new Source();
		source.setSourceId(alarm.getSource().getSourceId());
		source.setDescription(alarm.getSource().getDescription());
		source.setConnectionTimeout(new Integer(alarm.getSource().getConnectionTimeout()));
		source.setSurveillanceAlarmId(alarm.getSource().getSurveillanceAlarmId());
		source.setHostName(alarm.getSource().getHostName());
			
		ResponsiblePerson responsiblePersonSrc = new ResponsiblePerson (
				new Integer (alarm.getSource().getResponsiblePerson().getResponsibleId()),
				alarm.getSource().getResponsiblePerson().getFirstName(),
				alarm.getSource().getResponsiblePerson().getFamilyName(),
				alarm.getSource().getResponsiblePerson().getEMail(),
				alarm.getSource().getResponsiblePerson().getGsmNumber(),
				alarm.getSource().getResponsiblePerson().getPhoneNumber());
		source.setResponsiblePerson(responsiblePersonSrc);
		HashSet set = new HashSet();
		for (int pos=0; pos<alarm.getSource().getAlarmIds().length; pos++) {
			set.add(alarm.getSource().getAlarmIds()[pos]);
		}
		source.setAlarmIds(set);
		Timestamp lastContact = null;
		if (alarm.getSource().getSourceStatus().getLastContact()!=null) {
			lastContact = new Timestamp(alarm.getSource().getSourceStatus().getLastContact().getDate());
			lastContact.setNanos(alarm.getSource().getSourceStatus().getLastContact().getNanos());
		}
		SourceStatus sourceStatus = new SourceStatus(
				new Boolean(alarm.getSource().getSourceStatus().getConnected()),
				new Boolean(alarm.getSource().getSourceStatus().getEnabled()),
				lastContact);
		source.setStatus(sourceStatus);
		
		// The Status
		Timestamp sourceTimestamp = new Timestamp(alarm.getStatus().getSourceTimestamp().getDate());
		sourceTimestamp.setNanos(alarm.getStatus().getSourceTimestamp().getNanos());
		Timestamp userTimestamp= new Timestamp(alarm.getStatus().getUserTimestamp().getDate());
		userTimestamp.setNanos(alarm.getStatus().getUserTimestamp().getNanos());
		Timestamp systemTimestamp = new Timestamp(alarm.getStatus().getSystemTimestamp().getDate());
		systemTimestamp.setNanos(alarm.getStatus().getSystemTimestamp().getNanos());
		
		// The properties
		ACSProperty[] props = alarm.getStatus().getPersistentUserProperties();
		Properties properties = new Properties();
		for (int pos=0; pos<props.length; pos++) {
			properties.put(props[pos].getKey(),props[pos].getValue());
		}
		
		StatusImpl status = new StatusImpl(
				new Boolean(alarm.getStatus().getActive()),
				new Boolean(alarm.getStatus().getMasked()),
				new Boolean(alarm.getStatus().getReduced()),
				new Boolean(alarm.getStatus().getActivatedByBackup()),
			    new Boolean(alarm.getStatus().getTerminatedByBackup()),
			    alarm.getStatus().getSourceHostname(), 
			    sourceTimestamp, userTimestamp,
			    systemTimestamp, properties);
		
		// The Categories
		HashSet categories = new HashSet();
		for (int pos=0; pos<alarm.getCategoriesCount(); pos++) {
			ACSCategory cat = alarm.getCategories(pos);
			
			CategoryImpl catImpl = new CategoryImpl(
					new Integer(cat.getCategoryId()),
					cat.getName(),
					cat.getDescription(),
					cat.getPath(),
					cat.getIsLeaf());
			catImpl.setParentId(new Integer(cat.getParentId()));
			
			HashSet alarmIds = new HashSet();
			if (cat.getAlarmIdsCount()>0) {
				for (pos=0; pos<cat.getAlarmIdsCount(); pos++) {
					alarmIds.add(cat.getAlarmIds(pos));
				}
				catImpl.setAlarmIds(alarmIds);
			}
			HashSet childrenIds = new HashSet();
			if (cat.getChildrenIdsCount()>0) {
				for (pos=0; pos<cat.getChildrenIdsCount(); pos++) {
					childrenIds.add(cat.getChildrenIds(pos));
				}
				catImpl.setChildrenIds(childrenIds);
			}
			categories.add(catImpl);
		}
		
		// MutiplicityChildIds (added later to alarmImpl)
		HashSet MultiplicityChildIds = new HashSet();
		if (alarm.getMultiplicityChildrenIdsCount()>0) {
			for (int pos=0; pos<alarm.getMultiplicityChildrenIdsCount(); pos++) {
				MultiplicityChildIds.add(alarm.getMultiplicityChildrenIds(pos));
			}
		}
		// MutiplicityParentIds (added later to alarmImpl)
		HashSet MultiplicityParentIds = new HashSet();
		if (alarm.getMultiplicityParentIdsCount()>0) {
			for (int pos=0; pos<alarm.getMultiplicityParentIdsCount(); pos++) {
				MultiplicityParentIds.add(alarm.getMultiplicityParentIds(pos));
			}
		}
		// NodeChildIds (added later to alarmImpl)
		HashSet NodeChildIds = new HashSet();
		if (alarm.getNodeChildrenIdsCount()>0) {
			for (int pos=0; pos<alarm.getNodeChildrenIdsCount(); pos++) {
				NodeChildIds.add(alarm.getNodeChildrenIds(pos));
			}
		}
		// NodeParentIds (added later to alarmImpl)
		HashSet NodeParentIds = new HashSet();
		if (alarm.getNodeParentIdsCount()>0) {
			for (int pos=0; pos<alarm.getNodeParentIdsCount(); pos++) {
				NodeParentIds.add(alarm.getNodeParentIds(pos));
			}
		} 
		
		// The multiplicity threshold (added later to alarmImpl)
		int multiplityThreshold = alarm.getMultiplicityThreshold();
		
		
		AlarmImpl alarmImpl = new  AlarmImpl(alarmId, systemName, identifier,
				problemDescription, priority, cause,
				action, consequence, piquetGSM,
				piquetEmail, helpURLString, instant,
				source, location,
				responsiblePerson, categories,
				status, triplet,
				nodeParent,	multiplicityParent,
				nodeChild, multiplicityChild);
		alarmImpl.setNodeParentIds(NodeParentIds);
		alarmImpl.setNodeChildrenIds(NodeChildIds);
		alarmImpl.setMultiplicityChildrenIds(MultiplicityChildIds);
		alarmImpl.setMultiplicityParentIds(MultiplicityParentIds);
		alarmImpl.setMultiplicityThreshold(new Integer(multiplityThreshold));
		return alarmImpl;
	}
}
