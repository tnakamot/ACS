package jbaci.test;

import alma.ACS.ROdouble;
import alma.ACS.impl.CharacteristicComponentImpl;
import alma.acs.component.ComponentLifecycleException;
import alma.acs.container.ContainerServices;
import alma.jbaci.AlarmTestServerOperations;

public class AlarmTestServerImpl extends CharacteristicComponentImpl implements
		AlarmTestServerOperations {

	/**
	 * ROdouble property
	 */
	protected ROdouble doubleProp;
	
	/**
	 * @see alma.acs.component.ComponentLifecycle#initialize(alma.acs.container.ContainerServices)
	 */
	public void initialize(ContainerServices containerServices)
		throws ComponentLifecycleException {
		super.initialize(containerServices);
		
		try {
			// TODO instantiate DataAccess here.
		} catch (Throwable th) {
			throw new ComponentLifecycleException("Failed to create properties.", th);
		}
	}
	
	/**
	 * @see jbaci.test.AlarmTestServerOperations#doubleProp
	 */
	public ROdouble doubleProp() {
		return doubleProp;
	}
}
