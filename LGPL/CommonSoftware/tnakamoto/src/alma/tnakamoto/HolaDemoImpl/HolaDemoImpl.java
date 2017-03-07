package alma.tnakamoto.HolaDemoImpl;

import java.util.logging.Logger;

import org.omg.CORBA.DoubleHolder;
import org.omg.CORBA.IntHolder;

import alma.ACS.ComponentStates;
import alma.acs.component.ComponentLifecycle;
import alma.acs.container.ContainerServices;
import alma.tnakamoto.HolaDemoOperations;


public class HolaDemoImpl implements ComponentLifecycle, HolaDemoOperations
{
    private ContainerServices m_containerServices;
    private Logger m_logger;

	/////////////////////////////////////////////////////////////
	// Implementation of ComponentLifecycle
	/////////////////////////////////////////////////////////////
	
	public void initialize(ContainerServices containerServices) {
		m_containerServices = containerServices;
		m_logger = m_containerServices.getLogger();
		m_logger.info("initialize() called...");
	}
    
	public void execute() {
		m_logger.info("execute() called...");
	}
    
	public void cleanUp() {
		m_logger.info("cleanUp() called..., nothing to clean up.");
	}
    
	public void aboutToAbort() {
		cleanUp();
		m_logger.info("managed to abort...");
        System.out.println("HelloDemo component managed to abort... you should know this even if the logger did not flush correctly!");
	}

	/////////////////////////////////////////////////////////////
	// Implementation of ACSComponent
	/////////////////////////////////////////////////////////////
	
	public ComponentStates componentState() {
		return m_containerServices.getComponentStateManager().getCurrentState();
	}
	public String name() {
		return m_containerServices.getName();
	}

	/////////////////////////////////////////////////////////////
	// Implementation of HolaDemoOperations
	/////////////////////////////////////////////////////////////
	
	public String sayHola() {
		m_logger.info("sayHola called...");
		return "hola";
	}
	
	public String sayHolaWithParameters(String inString,
			DoubleHolder inoutDouble, IntHolder outInt) {
		m_logger.info("sayHello called with arguments inString=" + inString
				+ "; inoutDouble=" + inoutDouble.value
				+ ". Will return 'hello'...");
		outInt.value = (int) Math.round(Math.E * 10000000);
		return "hola";
	}
}