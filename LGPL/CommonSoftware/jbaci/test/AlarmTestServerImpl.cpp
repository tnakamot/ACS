#include "vltPort.h"

static char *rcsId="@(#) $Id$";
static void *use_rcsId = ((void)&use_rcsId,(void *) &rcsId);

#include "AlarmTestServerImpl.h"


using namespace jbaci::test;
using namespace baci;



AlarmTestServerImpl::AlarmTestServerImpl(const ACE_CString& name,
			     maci::ContainerServices * containerServices)
    : baci::CharacteristicComponentImpl(name, containerServices),
    m_doubleProp_p(0)
{
    AUTO_TRACE("AlarmTestServerImpl::AlarmTestServerImpl");

    m_doubleVal = 0.0;
    m_time1 = 134608945243381570;
    m_doubleDevIO = new TMCDB::MCtestDevIO<double>(m_doubleVal, m_time1);
    m_doubleProp_p  = new ROdouble(name+":doubleProp", getComponent(), m_doubleDevIO);
    CHARACTERISTIC_COMPONENT_PROPERTY(doubleProp, m_doubleProp_p);

}//AlarmTestServerImpl

AlarmTestServerImpl::~AlarmTestServerImpl()
{
    AUTO_TRACE("AlarmTestServerImpl::~AlarmTestServerImpl");
    if (m_doubleProp_p != 0)
    {
    	m_doubleProp_p->destroy();
    	m_doubleProp_p=0;
    }
    delete m_doubleDevIO;

}//~AlarmTestServerImpl


void AlarmTestServerImpl::execute()
{

}

void AlarmTestServerImpl::cleanUp()
{

}//cleanUp

ACS::ROdouble_ptr AlarmTestServerImpl::doubleProp ()
{
	if (m_doubleProp_p == 0)
	{
		return ACS::ROdouble::_nil();
	}

	ACS::ROdouble_var prop = ACS::ROdouble::_narrow(m_doubleProp_p ->getCORBAReference());
	return prop._retn();
}

/* --------------- [ MACI DLL support functions ] -----------------*/
#include <maciACSComponentDefines.h>
MACI_DLL_SUPPORT_FUNCTIONS(AlarmTestServerImpl)
/* ----------------------------------------------------------------*/


/*___oOo___*/
