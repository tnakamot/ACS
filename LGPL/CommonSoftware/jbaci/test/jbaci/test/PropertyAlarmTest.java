package jbaci.test;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManager;

import si.ijs.maci.Manager;
import si.ijs.maci.ManagerHelper;
import alma.ACSErr.CompletionHolder;
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
	private ORB orb;

	/**
	 * Root Portable Object Adapter (POA) object.
	 */
	private POA rootPOA;

	/**
	 * Property to be tested.
	 */
	private Manager manager;
	
	/**
	 * Name of AlarmTestServer component to be tested.
	 */
	private static final String COMPONENT_NAME = "ALARM_TEST_SRV";
	
	/**
	 * AlarmTestServer instance.
	 */
	private AlarmTestServer testServer;

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

		testServer = AlarmTestServerHelper.narrow(obj);
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		manager.release_component(0x05000000, COMPONENT_NAME);
		destroyCORBA();
	}

	public void testDouble() throws Throwable {
		CompletionHolder c = new CompletionHolder();
		double d = testServer.doubleProp().get_sync(c);
		assertEquals(d, -1.0);
	}

	public static TestSuite suite() {
		return new TestSuite(PropertyAlarmTest.class);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(PropertyAlarmTest.class);
		System.exit(0);
	}
}
