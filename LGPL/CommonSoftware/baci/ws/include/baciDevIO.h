#ifndef _BACI_DEVIO_H_
#define _BACI_DEVIO_H_
/*******************************************************************************
* ALMA - Atacama Large Millimiter Array
* (c) European Southern Observatory, 2003 
*
*This library is free software; you can redistribute it and/or
*modify it under the terms of the GNU Lesser General Public
*License as published by the Free Software Foundation; either
*version 2.1 of the License, or (at your option) any later version.
*
*This library is distributed in the hope that it will be useful,
*but WITHOUT ANY WARRANTY; without even the implied warranty of
*MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
*Lesser General Public License for more details.
*
*You should have received a copy of the GNU Lesser General Public
*License along with this library; if not, write to the Free Software
*Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
*
* "@(#) $Id: baciDevIO.h,v 1.99 2006/10/16 07:56:40 cparedes Exp $"
*
* who       when      what
* --------  --------  ----------------------------------------------
* bjeram 2003-06-02 renamed to DevIO
* bjeram 2002-03-20 changed methods to virtual (before they were pure virtual)
* bjeram  25/02/02  created
*/

/************************************************************************
 *
 *----------------------------------------------------------------------
 */

/** 
 * @file 
 * Header file BACI Device I/Os.
 */

#ifndef __cplusplus
#error This is a C++ include file and cannot be used from plain C
#endif


#include "baciErrTypeDevIO.h"  // we have to include this that exceptions can be thrown in backward compatiblity support code
// it can be removed when we remove "old" write and read

/**
 * Device I/O implementation class.
 * This class is a base class for providing input/output implementations to 
 * the Property class. (copied from ticsDevIO and converted into abstract class)
 *
 * @see Baci
 *
 *
 */

template <class T> class DevIO
{
public:
  /**
   * If true attempt to initialize the value at instanciation.
   * The value is set by the attribute in the CDB, by default this is false.
   */
    bool m_initialize;
 

    /**
     * Standard constructor
     */
    DevIO() {
	m_initialize=false;
	}
    /**
     * Destructor
     */
    virtual ~DevIO(){}
  
    /**
     * The return value of initializeValue function tells the constructor of property if it has to set initial value of DevIO to default value or not. 
     * The default value of property is read from CDB. 
     * InitializeValue: If true ACS will try to automatically set the
     * value upon initialization.
     * @return bool - If true attempt to initialize the value at instanciation.
     */

    virtual bool initializeValue(){
	return m_initialize;
    }

    /**
     * Method to read a value. It has to be override by implementation of DevIO
     * \param timestamp timestamp of reading
     * \exception any of ACS exceptions can be thrown
     * \return read value
     */
    virtual T read(ACS::Time& timestamp) throw (ACSErr::ACSbaseExImpl)
	{
	    int errcode;
	    T value = read(errcode, timestamp);
	    if (errcode==0)
		{
		return value;
		}
	    else
		{
		baciErrTypeDevIO::ReadErrorExImpl ex(__FILE__, __LINE__, "DevIO<>::read(deprecated)");
		ex.addData("Error Code from deprecated read", errcode);
		throw ex;
		}
	}//read
    
    /**
     * Method to write a value. It has to be override by implementation of DevIO
     * \param value value to be writen
     * \param timestamp timestamp of writing
     * \exception any of ACS exceptions can be thrown
     */
    virtual void write(const T& value, ACS::Time& timestamp) throw (ACSErr::ACSbaseExImpl)
	{
	    int errcode;
	    write(value, errcode, timestamp);
	    if (errcode!=0)
		{
		baciErrTypeDevIO::WriteErrorExImpl ex(__FILE__, __LINE__, "DevIO<>::write(deprecated)");
		ex.addData("Error Code from deprecated write", errcode);
		throw ex;
		}
	}//write

    /**
     * Method to read a value. It has to be override by implementation of DevIO
     * \deprecated The method is deprecated
     * \param errcode erro code. Old way. ACS Exceptions should be used.
     * \param timestamp timestamp of reading
     * \return read value
     */
    virtual T read(int& errcode, ACS::Time& timestamp)
	{
	    errcode = -1;
	    timestamp = 0ULL;
	    return T();
	}

    /**
     * Method to write a value. It has to be override by implementation of DevIO
     * \deprecated The method is deprecated
     * \param value value to be writen
     * \param errcode erro code. Old way. ACS Exceptions should be used.
     * \param timestamp timestamp of writing
     */
    virtual void write(const T& value, int& errcode, ACS::Time& timestamp)
	{
	    do {/* null */} while (&value == 0); // suppress unused warining
	    errcode = -1;
	    timestamp = 0ULL;
	}
};

#endif 

