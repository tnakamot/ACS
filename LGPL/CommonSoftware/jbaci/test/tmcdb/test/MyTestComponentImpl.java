package tmcdb.test;

import alma.ACS.ROdouble;
import alma.ACS.ROdoubleHelper;
import alma.ACS.ROdoublePOATie;
import alma.ACS.impl.CharacteristicComponentImpl;
import alma.ACS.impl.ROdoubleImpl;
import alma.ACS.jbaci.DataAccess;
import alma.TMCDB.MyTestComponentOperations;
import alma.acs.component.ComponentLifecycleException;
import alma.acs.container.ContainerServices;


public class MyTestComponentImpl extends CharacteristicComponentImpl implements MyTestComponentOperations {
    private ROdouble doubleProp;
    private ROdoubleImpl doublePropImpl;

    @Override
    public void initialize(ContainerServices containerServices) throws ComponentLifecycleException {
	super.initialize(containerServices);

	{
	    DataAccess<Double> doubleROPropDA = new MCTestDataAccess<Double>(3.14, 0L);
	    doublePropImpl = new ROdoubleImpl("doubleProp", this, doubleROPropDA);
	    ROdoublePOATie currentTie = new ROdoublePOATie(doublePropImpl);
	    doubleProp = ROdoubleHelper.narrow(this.registerProperty(doublePropImpl, currentTie));
	}
    }

    @Override
    public ROdouble doubleProp() {
	return doubleProp;
    }
}