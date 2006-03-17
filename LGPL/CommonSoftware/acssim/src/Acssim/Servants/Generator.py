# @(#) $Id: Generator.py,v 1.38 2006/03/17 20:41:31 dfugate Exp $
#
# Copyright (C) 2001
# Associated Universities, Inc. Washington DC, USA.
#
# Produced for the ALMA project
#
# This library is free software; you can redistribute it and/or modify it under
# the terms of the GNU Library General Public License as published by the Free
# Software Foundation; either version 2 of the License, or (at your option) any
# later version.
#
# This library is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
# details.
#
# You should have received a copy of the GNU Library General Public License
# along with this library; if not, write to the Free Software Foundation, Inc.,
# 675 Massachusetts Ave, Cambridge, MA 02139, USA.  Correspondence concerning
# ALMA should be addressed as follows:
#
# Internet email: alma-sw-admin@nrao.edu
# "@(#) $Id: Generator.py,v 1.38 2006/03/17 20:41:31 dfugate Exp $"
#
# who       when        what
# --------  ----------  -------------------------------------------------------
# dfugate   2003/12/09  Created.
#------------------------------------------------------------------------------
'''

TODO LIST:

'''
#--REGULAR IMPORTS-------------------------------------------------------------


from random  import randrange
from random  import choice
from new     import instance
#--CORBA STUBS-----------------------------------------------------------------
import CORBA
import ACSErr
import ACS
#--ACS Imports-----------------------------------------------------------------
from Acspy.Common.Log       import getLogger

from Acspy.Common.TimeHelper import getTimeStamp

from Acssim.Servants.Goodies           import *
#--GLOBALS---------------------------------------------------------------------

#------------------------------------------------------------------------------
def getRandomValue(typeCode, compRef):
    '''
    TODO:
    - complete me!
    '''
    #Determine the value type first. This is really just an enumeration for the
    #CORBA typecode
    if not isinstance(typeCode, CORBA.AttributeDescription):
        #just for methods
        getLogger("Acssim.Servants.Generator").logTrace("Dealing with a CORBA AttributeDescription:" + str(typeCode))
        valType = typeCode.kind()
    else:
        valType = typeCode.type.kind()
        
    #--------------------------------------------------------------------------
    #First check to see if valType is a simple CORBA type we can immediately return.
    #If this is the case it's just returned...otherwise an exception is thrown and...
    try:
        return getRandomSimpleValue(valType)
    except:
        #we move on
        getLogger("Acssim.Servants.Generator").logTrace("Not a simple CORBA Type:" + str(valType))
    #--------------------------------------------------------------------------
    #if it's a sequence, array, etc...this is a very special case:
    try:
        return getRandomTuple(typeCode, compRef, valType)
    except CORBA.NO_IMPLEMENT, e:
        raise e
    except:
        getLogger("Acssim.Servants.Generator").logTrace("Wasn't an alias-related type:" + str(valType))
    #--------------------------------------------------------------------------
    if valType == CORBA.tk_objref:
        getLogger("Acssim.Servants.Generator").logTrace("Return value is a CORBA object:" + str(valType))
        return getRandomCORBAObject(typeCode, compRef)
    elif valType == CORBA.tk_enum:
        return getRandomEnum(typeCode)
    elif valType == CORBA.tk_alias:
        if not isinstance(typeCode, CORBA.AttributeDescription):
            #just for methods
            return getRandomValue(typeCode.content_type(), compRef)
        else:
            return getRandomValue(typeCode.type.content_type(), compRef)
    elif valType == CORBA.tk_null:
        getLogger("Acssim.Servants.Generator").logTrace("Encountered null return value")
        return None
    #--------------------------------------------------------------------------
    elif valType == CORBA.tk_struct:

        if not isinstance(typeCode, CORBA.AttributeDescription):
            #just for methods
            #first get the struct definition from the IFR
            structDef =getDefinition(typeCode.id())
        else:
            #must be an attribute
            structDef =getDefinition(typeCode.type.id())

        try:
            return getKnownBaciType(structDef._get_id())
        except:
            pass
        
        #determine which Python package the struct is in...
        #changes 'IDL:alma/someMod/.../struct:1.0" to [ 'someMod', ..., 'struct' ]
        packageName = structDef._get_id().split(':')[1].split('/')[1:]

        #Just the 'struct' part...
        structName = packageName.pop()
            
        #convert the list to a stringified module/package structure
        packageName = reduce((lambda x, y : str(x) + '.' + str(y)), packageName)

        getLogger("Acssim.Servants.Generator").logTrace("structName=" + str(structName) +
                             "; packageName=" + str(packageName))
        
        #import the proper module containing the struct defintion
        tGlobals = {}
        tLocals = {}
        #module object where the struct is contained
        tModule = __import__(packageName, tGlobals, tLocals, [structName])
        #class object for the struct
        tClass = tModule.__dict__.get(structName)

        #create an instance of the struct using a kooky Python mechanism.
        retVal = instance(tClass)

        #populate the fields of the struct using the IFR
        for member in structDef._get_members():
            getLogger("Acssim.Servants.Generator").logTrace("Adding a member variable for: " + str(member.name))
            retVal.__dict__[member.name] = getRandomValue(member.type_def._get_type(), compRef)

        return retVal
    #--------------------------------------------------------------------------
    elif valType == CORBA.tk_Principal:
        getLogger("Acssim.Servants.Generator").logCritical(str(valType) + " not yet supported")
        raise CORBA.NO_IMPLEMENT()
    elif valType == CORBA.tk_TypeCode:
        getLogger("Acssim.Servants.Generator").logCritical(str(valType) + " not yet supported")
        raise CORBA.NO_IMPLEMENT()
    elif valType == CORBA.tk_abstract_interface:
        getLogger("Acssim.Servants.Generator").logCritical(str(valType) + " not yet supported")
        raise CORBA.NO_IMPLEMENT()
    elif valType == CORBA.tk_any:
        getLogger("Acssim.Servants.Generator").logCritical(str(valType) + " not yet supported")
        raise CORBA.NO_IMPLEMENT()
    elif valType == CORBA.tk_except:
        getLogger("Acssim.Servants.Generator").logCritical(str(valType) + " not yet supported")
        raise CORBA.NO_IMPLEMENT()
    elif valType == CORBA.tk_fixed:
        getLogger("Acssim.Servants.Generator").logCritical(str(valType) + " not yet supported")
        raise CORBA.NO_IMPLEMENT()
    elif valType == CORBA.tk_local_interface:
        getLogger("Acssim.Servants.Generator").logCritical(str(valType) + " not yet supported")
        raise CORBA.NO_IMPLEMENT()
    elif valType == CORBA.tk_native:
        getLogger("Acssim.Servants.Generator").logCritical(str(valType) + " not yet supported")
        raise CORBA.NO_IMPLEMENT()

    elif valType == CORBA.tk_union:
        getLogger("Acssim.Servants.Generator").logCritical(str(valType) + " not yet supported")
        raise CORBA.NO_IMPLEMENT()
    elif valType == CORBA.tk_value:
        getLogger("Acssim.Servants.Generator").logCritical(str(valType) + " not yet supported")
        raise CORBA.NO_IMPLEMENT()

    elif valType == CORBA.tk_wchar:
        getLogger("Acssim.Servants.Generator").logCritical(str(valType) + " not yet supported")
        raise CORBA.NO_IMPLEMENT()
    elif valType == CORBA.tk_wstring:
        getLogger("Acssim.Servants.Generator").logCritical(str(valType) + " not yet supported")
        raise CORBA.NO_IMPLEMENT()
    else:
        getLogger("Acssim.Servants.Generator").logCritical(str(valType) + " not yet supported")
        raise CORBA.NO_IMPLEMENT()

#------------------------------------------------------------------------------
def getDefinition(irLabel):
    '''
    Helper function returns the interface definition of some IDL type given
    its complete interface repository location.

    Parameters: irLabal is the location of the interface defintion within the
    IFR. Typically this is something like "IDL:alma/someModule/someInterface:1.0"

    Returns: the IFR definition of irLabel

    Raises: ???
    '''
    global IR
    interf = IR.lookup_id(irLabel)
    return interf
#------------------------------------------------------------------------------
def getKnownBaciType(structName):
    '''
    '''
    if structName=="IDL:alma/ACSErr/Completion:1.0":
        return ACSErr.Completion(long(getTimeStamp().value),  #unsigned long long timeStamp;
                                 0L,  #ACSErr::CompletionType type;
                                 0L,  #ACSErr::CompletionCode code;
                                 ())
    else:
        raise "dummie exception"

#------------------------------------------------------------------------------
def getRandomSimpleValue(valType):
    '''
    Helper function returns a random value of (typecode) valType or throws an exception
    if valType is not a simple type. Sample usage could be:

       getRandomSimpleValue(CORBA.tk_boolean)

    Parameters: valType is a typeCode

    Returns: a random value of type typeCode

    Raises: an exception if valType is not really a simple CORBA type
    '''
    if valType == CORBA.tk_boolean:
        #randomly returns 0 or 1
        retVal = randrange(0,100) % 2
    
    elif valType == CORBA.tk_char:
        #randomly returns some predetermined character
        retVal = choice(getCHARS())
    
    elif valType == CORBA.tk_octet:
        #returns 0-255
        retVal = randrange(0,256)
    
    elif valType == CORBA.tk_short:
        #returns a random short
        retVal = randrange(-(2**15), (2**15) - 1)
    elif valType == CORBA.tk_ushort:
        #returns a random unsigned short
        retVal = randrange(0, (2**16) - 1)
    
    elif valType == CORBA.tk_long:
        #returns a random long
        retVal = randrange(-(2**31), (2**31) - 1)
    elif valType == CORBA.tk_ulong:
        #returns a random unsigned long
        retVal = randrange(0, (2**32) - 1)

    elif valType == CORBA.tk_longlong:
        #returns a random long long
        retVal = randrange(-(2**63), (2**63) - 1)
    elif valType == CORBA.tk_ulonglong:
        #returns a random unsigned long long
        retVal = randrange(0, (2**64) - 1)
    
    elif valType == CORBA.tk_float:
        #DWF-make this really go through a float's entire range of values
        retVal = eval(str(getRandomSimpleValue(CORBA.tk_short)) + '.' +
                    str(getRandomSimpleValue(CORBA.tk_octet)))
    
    elif valType == CORBA.tk_double:
        #returns a random float plus more digits
        retVal =  getRandomSimpleValue(CORBA.tk_float) * 1000.0

    elif valType == CORBA.tk_longdouble:
        #DWF-CORBA IDL->Py mapping specifies a CORBA.long_double() object
        #to be used in cases like these. Unfortunately omniORB does not
        #currently support it.
        getLogger("Acssim.Servants.Generator").logDebug("long doubles not supported by omniORBPy")
        return 3.1415926535897931

    elif valType == CORBA.tk_string:
        retVal = "..." + str(getRandomSimpleValue(CORBA.tk_double)) + "..."

    elif valType == CORBA.tk_void:
        retVal = None
    elif valType == CORBA.tk_null:
        retVal = None
    else:
        raise CORBA.NO_IMPLEMENT()

    return retVal
#------------------------------------------------------------------------------
def getRandomCORBAObject(typeCode, compRef):
    '''
    Helper function returns a random CORBA object.

    Parameters:
    - typeCode is the typecode of the object to be created
    - compRef is an ACSComponent which will activate the newly created
    CORBA object

    Returns: the newly created CORBA object

    Raises: ???
    '''
    #has to be in this method unfortunately to avoid cyclic dependencies.
    from Acssim.Servants.Simulator import BaseSimulator
    getLogger("Acssim.Servants.Generator").logTrace("Creating a random CORBA object")
    
    try:
        #for methods...
        irLabel = typeCode.id()
    except:
        #...and attributes
        irLabel = typeCode.type.id()

    if not isinstance(typeCode, CORBA.AttributeDescription):
        #just for methods
        objName = irLabel.replace(":1.0","").replace("IDL:", "").replace("/", ".")
    else:
        objName = typeCode.name

    getLogger("Acssim.Servants.Generator").logTrace("CORBA object type is:" + str(irLabel))
    getLogger("Acssim.Servants.Generator").logTrace("CORBA object name is:" + str(objName))

    #create a new simulated object
    retVal = BaseSimulator(irLabel, compRef._get_name() + objName)

    #activate it as a CORBA object...        
    retVal = compRef.activateOffShoot(retVal)

    #this must be done so that the newly created CORBA object can also activate
    #offshoots on its own
    retVal.activateOffShoot = compRef.activateOffShoot

    return retVal
#------------------------------------------------------------------------------
def getRandomEnum(typeCode):
    '''
    Helper function returns a random enumueration based on the typecode provided.

    Parameters: type code is quite literally a CORBA typecode

    Returns: a random enumeration of the type specified by typeCode

    Raises: ???
    '''
    getLogger("Acssim.Servants.Generator").logTrace("Dealing with an enum")
    
    #Determine the value type first. This is really just an enumeration for the
    #CORBA typecode
    if not isinstance(typeCode, CORBA.AttributeDescription):
        #just for methods
        #first get the enumeration definition
        enumDef =getDefinition(typeCode.id())
    else:
        #first get the enumeration definition
        enumDef =getDefinition(typeCode.type.id())
        
    #determine which Python package the enum is in...
    #changes 'IDL:alma/someMod/.../enumeration:1.0" to [ 'someMod', ..., 'enumeration' ]
    enumName = enumDef._get_id().split(':')[1].split('/')[1:]
    modName = enumName[0]

    #convert the list to a stringified Python package structure which can
    #be used with eval
    enumName = reduce((lambda x, y : str(x) + '.' + str(y)), enumName)

    getLogger("Acssim.Servants.Generator").logTrace("enum name is:" + str(enumName))
    getLogger("Acssim.Servants.Generator").logTrace("enum module is:" + str(modName))
    
    #now comes the complicated part...importing the correct CORBA stub
    #without polluting the local namespace...
    tGlobals = {}
    tLocals  = {}
    exec "from random import choice" in tGlobals, tLocals
    exec "import " + modName in tGlobals, tLocals
    
    #with any luck, we should now be able to return the enumeration value
    #without any problems
    retVal = eval("choice(" + enumName + "._items)", tGlobals, tLocals)
    getLogger("Acssim.Servants.Generator").logTrace("enum return value is:" + str(retVal))
    return retVal
#------------------------------------------------------------------------------
def getRandomTuple(typeCode, compRef, valType):
    '''
    Helper function returns a random Python tuple for CORBA sequences and array
    types.

    Parameters:
    - type code is quite literally a CORBA typecode
    - compRef is a reference to a component used to activated IDL OffShoot interfaces
    - valType is the value type of the random value we are trying to get

    Returns: a random enumeration of the type specified by typeCode

    Raises: an (unknown) exception if the typecode does not really specify a list type to
    be returned or a CORBA.NO_IMPLEMENT if we have not gotten around to supporting
    the specific typecode yet (e.g., value boxes).
    '''
    #if this next block does not throw an exception...
    if not isinstance(typeCode, CORBA.AttributeDescription):
        #just for methods
        realValType  = typeCode.content_type().kind()
        realTypeCode = getDefinition(typeCode.id())._get_original_type_def()._get_element_type()
    else:
        realValType  = typeCode.type.content_type().kind()
        realTypeCode = getDefinition(typeCode.type.id())._get_original_type_def()._get_element_type()
    #we're really dealing with a sequence, array, value_box, or an alias
    getLogger("Acssim.Servants.Generator").logTrace("Dealing with a sequence, array, value_box, or alias:" +
                         str(realValType) + " " + str(realTypeCode))

    #Sequence
    if realValType == CORBA.tk_sequence:
        getLogger("Acssim.Servants.Generator").logTrace("Dealing with a sequence.")
        retVal = []
        for i in range(0,randrange(0, getMaxSeqSize())):
            retVal.append(getRandomValue(realTypeCode, compRef))
        #Sequences of octects and characters must be handled specially in Python
        if realTypeCode.kind()==CORBA.tk_octet or realTypeCode.kind()==CORBA.tk_char:
            getLogger("Acssim.Servants.Generator").logTrace("Dealing with a sequence of characters/octets.")
            return reduce((lambda x, y : str(x) + str(y)), retVal)
        else:
            return tuple(retVal)

    #Array
    #DWF-take into consideration multi-dimensional arrays
    elif realValType == CORBA.tk_array:
        #First determine size of the array
        if not isinstance(typeCode, CORBA.AttributeDescription):
            size = getDefinition(typeCode.id())._get_original_type_def()._get_type().length()
        else:
            size = getDefinition(typeCode.type.id())._get_original_type_def()._get_type().length()

        getLogger("Acssim.Servants.Generator").logTrace("Dealing with an array of size:" + str(size))
        retVal = []
        for i in range(0,size):
            retVal.append(getRandomValue(realTypeCode, compRef))
            
        #Sequences of octects and characters must be handled specially in Python
        if realTypeCode.kind()==CORBA.tk_octet or realTypeCode.kind()==CORBA.tk_char:
            getLogger("Acssim.Servants.Generator").logTrace("Dealing with an array of characters/octets.")
            return reduce((lambda x, y : str(x) + str(y)), retVal)
        else:
            return tuple(retVal)
            
    #Value Box
    elif realValType == CORBA.tk_value_box:
        getLogger("Acssim.Servants.Generator").logCritical("value_box not yet supported")
        raise CORBA.NO_IMPLEMENT()

    #If this block of code can ever really be executed in practice...I'll be amazed!
    elif valType == CORBA.tk_alias:
        if not isinstance(typeCode, CORBA.AttributeDescription):
            #just for methods
            return getRandomValue(realTypeCode, compRef)
        else:
            return getRandomValue(realTypeCode, compRef)

#------------------------------------------------------------------------------
def tryCallbackParams(params, compRef):
    '''
    '''
    compl = ACSErr.Completion(long(getTimeStamp().value),  #unsigned long long timeStamp;
                              0L,  #ACSErr::CompletionType type;
                              0L,  #ACSErr::CompletionCode code;
                              ())
    #compl = getRandomValue(ACSErr._tc_Completion, compRef)


    cbId = 0L    #default value
    for param in params:
        if isinstance(param, ACS.CBDescIn):
            cbId = param.id_tag
            break
        
    cbDescOut = ACS.CBDescOut(0L, cbId)
    #cbDescOut = getRandomValue(ACS._tc_CBDescOut, compRef)

    for param in params:
        if isinstance(param, ACS._objref_CBvoid):
            param.done(compl,
                       cbDescOut)
        elif isinstance(param, ACS._objref_CBpattern):
            param.done(getRandomValue(ACS._tc_pattern, compRef),
                       compl,
                       cbDescOut)
        elif isinstance(param, ACS._objref_CBpattern):
            param.done(getRandomValue(ACS._tc_pattern, compRef),
                       compl,
                       cbDescOut)
        elif isinstance(param, ACS._objref_CBdouble):
            param.done(getRandomValue(CORBA._tc_double, compRef),
                       compl,
                       cbDescOut)
        elif isinstance(param, ACS._objref_CBstring):
            param.done(getRandomValue(CORBA._tc_string, compRef),
                       compl,
                       cbDescOut)
        elif isinstance(param, ACS._objref_CBstringSeq):
            param.done(getRandomValue(ACS._tc_stringSeq, compRef),
                       compl,
                       cbDescOut)
        elif isinstance(param, ACS._objref_CBlong):
            param.done(getRandomValue(CORBA._tc_long, compRef),
                       compl,
                       cbDescOut)
        elif isinstance(param, ACS._objref_CBdoubleSeq):
            param.done(getRandomValue(ACS._tc_doubleSeq, compRef),
                       compl,
                       cbDescOut)
        elif isinstance(param, ACS._objref_CBlongSeq):
            param.done(getRandomValue(ACS._tc_longSeq, compRef),
                       compl,
                       cbDescOut)
        else:
            pass
#-----------------------------------------------------------------------
if __name__=="__main__":
    #This just exists for testing purposes. Should be removed later.
    from time import sleep
    sleep(100)
    







