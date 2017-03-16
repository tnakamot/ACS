package jbaci.test;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManager;

import si.ijs.maci.Manager;
import si.ijs.maci.ManagerHelper;
import alma.acs.util.ACSPorts;
import alma.jbaci.AlarmTestServer;
import alma.jbaci.AlarmTestServerHelper;

/**
 * @author <a href="mailto:takashi.nakamotoATnao.ac.jp">Takashi Nakamoto</a>
 * @version $id$
 */
public class PropertyAlarmTest extends TestCase {

	/**
	 * Object Request Broker (ORB) object.
	 */
	private ORB orb = null;

	/**
	 * Root Portable Object Adapter (POA) object.
	 */
	private POA rootPOA = null;

	/**
	 * Property to be tested.
	 */
	private Manager manager = null;

	/**
	 * Name of AlarmTestServer component to be tested.
	 */
	private static final String COMPONENT_NAME = "ALARM_TEST_SRV";

	/**
	 * Initialize CORBA.
	 */
	private void initCORBA() throws Exception {
		orb = ORB.init(new String[0], System.getProperties());

		// resolve RootPOA
		rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

		// activate POA
		POAManager manager = rootPOA.the_POAManager();
		manager.activate();

		// spawn ORB thread
		new Thread(new Runnable() {
			public void run() {
				orb.run();
			}
		}, "CORBA").start();
	}

	/**
	 * Overloads the destroy to first perform a ORB shutdown.
	 */
	public void destroyCORBA() {
		// destory ORB
		if (orb != null) {
			// do not wait for completion
			orb.shutdown(false);

			// and finally destroy
			orb.destroy();
		}
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		initCORBA();

		manager = ManagerHelper.narrow(orb.string_to_object("corbaloc::"
				+ ACSPorts.getIP() + ":" + ACSPorts.getManagerPort()
				+ "/Manager"));
		org.omg.CORBA.Object obj = manager.get_component(0x05000000,
				COMPONENT_NAME, true);

		AlarmTestServer ats = AlarmTestServerHelper.narrow(obj);
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		manager.release_component(0x05000000, COMPONENT_NAME);
		destroyCORBA();
	}

	public void testA() throws Throwable {
		assertEquals("a", "a");
	}

	public static TestSuite suite() {
		return new TestSuite(PropertyAlarmTest.class);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(PropertyAlarmTest.class);
		System.exit(0);
	}
}
