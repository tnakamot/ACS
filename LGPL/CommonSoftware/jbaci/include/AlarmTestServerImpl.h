#ifndef ALARM_TEST_SERVER_IMPL_H
#define ALARM_TEST_SERVER_IMPL_H

#ifndef __cplusplus
#error This is a C++ include file and cannot be used from plain C
#endif

#include "AlarmTestServerS.h"
#include "MCtestDevIO.h"
#include <baciCharacteristicComponentImpl.h>
#include <baciROdouble.h>

namespace jbaci
{
namespace test
{

class AlarmTestServerImpl :
	public baci::CharacteristicComponentImpl,
	public POA_jbaci::AlarmTestServer
{
public:
	AlarmTestServerImpl(const ACE_CString& name,
			maci::ContainerServices * containerServices);

	~AlarmTestServerImpl();

	void execute();

	// componnet's life cycle
	void cleanUp();


	// implementations of IDL's methods
	 ACS::ROdouble_ptr doubleProp ();

private:


	    baci::ROdouble *m_doubleProp_p;
	    double m_doubleVal;
	    ACS::Time m_time1;
	    TMCDB::MCtestDevIO<double> *m_doubleDevIO;

	    /**
	     * ALMA C++ coding standards state copy operators should be disabled.
	     */
	    void operator=(const AlarmTestServerImpl&);

};//class AlarmTestServerImpl

};//namespace test
};//namespace jbaci


#endif /*!_H*/
