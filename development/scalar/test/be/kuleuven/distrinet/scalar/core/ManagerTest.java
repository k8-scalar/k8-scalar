package be.kuleuven.distrinet.scalar.core;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.junit.runners.MethodSorters;

import be.kuleuven.distrinet.scalar.config.Option;
import be.kuleuven.distrinet.scalar.data.TestingDataProvider;
import be.kuleuven.distrinet.scalar.data.TestingDataProviderConnector;
import be.kuleuven.distrinet.scalar.exceptions.DataException;
import be.kuleuven.distrinet.scalar.exceptions.InternalException;
import be.kuleuven.distrinet.scalar.exceptions.ScalarExecutionException;
import be.kuleuven.distrinet.scalar.exceptions.ScalarInstantiationException;
import be.kuleuven.distrinet.scalar.experiment.Experiment;
import be.kuleuven.distrinet.scalar.testing.TestUtils;
import be.kuleuven.distrinet.scalar.testing.TimingTestUser;
import be.kuleuven.distrinet.scalar.testing.TimingTester;
import be.kuleuven.distrinet.scalar.users.DistributedTestUser;
import be.kuleuven.distrinet.scalar.users.TestUser;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ManagerTest {

	@Before
	public void init() {
		TestingDataProviderConnector.instance().reset();
		TimingTester.init();
	}

	@Test
	public void goodConstruction() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {		
		Manager mgr = new Manager(TestUtils.getMinimalTestConfig());
		assertTrue(mgr != null);
		assertFalse(mgr.finished());
		assertFalse(mgr.finishedSuccessfully());
		mgr.waitUntilFinished();
		assertTrue(mgr.finished());
		assertTrue(mgr.finishedSuccessfully());
	}

	@Test(expected=ScalarInstantiationException.class)
	public void badConstruction() throws ScalarInstantiationException, ScalarExecutionException {
		Properties bad = new Properties();

		Manager mgr = new Manager(null);
		assertTrue(mgr == null);

		mgr = new Manager(bad);
		assertTrue(mgr == null);
	}

	@Test
	public void simpleExperiment() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException, DataException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("user_peak_load", "1");
		props.put("user_peak_duration", "1");

		Manager mgr = new Manager(props);
		assertTrue(mgr != null);

		mgr.waitUntilFinished();

		assertTrue(mgr.finished()); 
		assertTrue(mgr.getResult().isPresent());
		assertTrue(mgr.finishedSuccessfully());
	}

	@Test
	public void clusteringWith2() throws ScalarInstantiationException, ScalarExecutionException, DataException, InterruptedException {
		String load = "1,500,1000";

		Properties props = TestUtils.getMinimalTestConfig();
		props.put("scalar_minimal_cluster_size", "2");
		props.put("user_peak_load", load);
		props.put("user_peak_duration", "1");
		props.put("plugins", props.get("plugins") + 
				",be.kuleuven.distrinet.scalar.plugin.ClusterMonitor");

		Manager master = new Manager(props);	

		Thread.sleep(1000);

		Manager other = new Manager(props);

		assertFalse(master.finished());
		assertFalse(other.finished());

		other.waitUntilFinished();
		master.waitUntilFinished();

		assertTrue(master.isMaster());
		assertFalse(other.isMaster());

		assertTrue(master.finishedSuccessfully());
		assertTrue(other.finishedSuccessfully());
	}

	@Test
	public void clusteringWith3() throws ScalarInstantiationException, ScalarExecutionException, DataException, InterruptedException {
		String load = "1,500,1000";

		Properties props = TestUtils.getMinimalTestConfig();
		props.put("scalar_minimal_cluster_size", "3");
		props.put("user_peak_load", load);
		props.put("user_peak_duration", "1");
		props.put("plugins", props.get("plugins") + 
				",be.kuleuven.distrinet.scalar.plugin.ClusterMonitor");

		Manager master = new Manager(props);	

		Thread.sleep(1000);

		Manager other = new Manager(props);
		Manager third = new Manager(props);

		assertFalse(master.finished());
		assertFalse(other.finished());
		assertFalse(third.finished());

		other.waitUntilFinished();
		third.waitUntilFinished();
		master.waitUntilFinished();

		assertTrue(master.isMaster());
		assertFalse(other.isMaster());
		assertFalse(third.isMaster());

		assertTrue(master.finishedSuccessfully());
		assertTrue(other.finishedSuccessfully());
		assertTrue(third.finishedSuccessfully());
	}

	@Test
	public void masterElectionRace() throws ScalarInstantiationException, ScalarExecutionException, DataException, InterruptedException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("scalar_minimal_cluster_size", "5");

		ArrayList<Manager> managers = new ArrayList<Manager>();
		for (int i = 0; i < 5; i++) {
			managers.add(new Manager(props));
		}

		for (Manager m : managers) {
			m.waitUntilFinished();
		}

		int masterCount = 0;
		int successCount = 0;
		for (Manager m : managers) {
			try {
				if (m.isMaster()) masterCount++;
			} catch (InternalException e) {
				fail(e.getLocalizedMessage());
			}
			if (m.finishedSuccessfully()) successCount++;
		}

		assertTrue("Too many masters!", masterCount == 1);
		assertTrue("Too many failures!", successCount == 5);
	}

	@Test
	public void userIDGenerationTest() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Properties config = TestUtils.getMinimalTestConfig();

		config.put(Option.MAX_USER_ID.toString(), "99");
		config.put("user_implementations", "be.kuleuven.distrinet.scalar.testing.TimingTestUser:1");
		config.put(Option.USER_PEAK_LOAD.toString(), "100");
		config.put("think_time", "500");
		config.put("user_peak_duration", "2");
		config.put(Option.LOGIN_ACCOUNT.toString(), "#ID#");
		config.put("scalar_minimal_cluster_size", "3");

		TimingTester.init();
		TimingTester ttester = TimingTester.instance();

		Manager master = new Manager(config);
		Manager other = new Manager(config);
		Manager slave = new Manager(config);

		master.waitUntilFinished();
		slave.waitUntilFinished();
		other.waitUntilFinished();

		assertEquals(100, ttester.users().size());

		ArrayList<Integer> ids = new ArrayList<Integer>();
		for (TimingTestUser u : ttester.users()) {
			try {
				ids.add(Integer.parseInt(u.username()));
			} catch (NullPointerException e) {
				fail("Problem with username " + u.username());
			}
		}

		assertEquals(100, ids.size());

		Collections.sort(ids);

		for (int i = 0; i < 100; i++) {
			assertTrue(ids.get(i) == i);
		}

		for (TimingTestUser u : ttester.users()) {
			ids.removeIf(i -> i == Integer.parseInt(u.username()));
		}

		for (int i : ids) {
			fail("Remaining ID " + i);
		}
	}

	@Test
	public void userSpawnerTest1() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("user_peak_duration", "1");
		spawnAndTest(props, 100, 43, 29, 28);
	}

	@Test
	public void userSpawnerTest2() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("user_peak_duration", "1");
		spawnAndTest(props, 100, 2, 9, 13);
	}

	@Test
	public void userSpawnerTest3() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("user_peak_duration", "1");
		spawnAndTest(props, 100, 29, 13, 0);
	}

	public void spawnAndTest(Properties props, int peakLoad, int testUserNb, int distributedTestUserNb, int timingTestUserNb) throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		String usr1 = "be.kuleuven.distrinet.scalar.users.TestUser:" + ((double)testUserNb) * 0.01;
		String usr2 = "be.kuleuven.distrinet.scalar.users.DistributedTestUser:" + ((double)distributedTestUserNb) * 0.01;
		String usr3 = "be.kuleuven.distrinet.scalar.testing.TimingTestUser:" + ((double)timingTestUserNb) * 0.01;
		props.put("user_implementations", usr1 + "," + usr2 + "," + usr3);
		props.put("user_peak_load", "" + peakLoad);

		TimingTester.init();

		Manager mgr = new Manager(props);
		assertTrue(mgr != null);

		mgr.waitUntilFinished();

		int testUserCt = 0;
		int distributedTestUserCt = 0;
		int timingTestUserCt = 0;
		for (User u : mgr.userPool().users()) {
			if (u instanceof TestUser) testUserCt++;
			if (u instanceof DistributedTestUser) distributedTestUserCt++;
			if (u instanceof TimingTestUser) timingTestUserCt++;
		}

		int totalRatio = testUserNb + distributedTestUserNb + timingTestUserNb;

		assertEquals(peakLoad, testUserCt + distributedTestUserCt + timingTestUserCt);
		assertEquals(Math.round(((double)testUserNb) * ((double)peakLoad) / ((double) totalRatio)), testUserCt);
		assertEquals(Math.round(((double)distributedTestUserNb) * ((double)peakLoad) / ((double) totalRatio)), distributedTestUserCt);
		assertEquals(Math.round(((double)timingTestUserNb) * ((double)peakLoad) / ((double) totalRatio)), timingTestUserCt);
	}

	@Test
	public void allUsersHaveStopped() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("user_peak_load", "10000");
		props.put("user_peak_duration", "1");
		props.put("plugins", "be.kuleuven.distrinet.scalar.testing.UserStopTestPlugin");
		props.put("user_implementations", "be.kuleuven.distrinet.scalar.testing.StopTestUser:1");

		Manager mgr = new Manager(props);
		assertTrue(mgr != null);

		mgr.waitUntilFinished();

		try {
			assertTrue((boolean)TestingDataProviderConnector.instance().primary().get("testing-successful"));
		} catch (DataException e) {
			fail(e.getLocalizedMessage());
		}
	}

	// XXX hangs the unit test progression
	//@Test(timeout=7000)
	public void vmShutdownHookTest() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("user_peak_load", "100");
		props.put("user_peak_duration", "100");
		props.put("scalar_minimal_cluster_size", "1");
		props.put("internal_debug_enabled", "true");
		props.put("debug_filename", "junit_shutdown.txt");
		
		Manager mgr = new Manager(props);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
				}

				System.exit(0);
			}
		}).start();

		mgr.waitUntilFinished();
		assertTrue(mgr.finished());
	}

	@Test(timeout=7000)
	public void shutdownBehavior1() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("user_peak_load", "100");
		props.put("user_peak_duration", "100");
		props.put("scalar_minimal_cluster_size", "1");

		Manager mgr = new Manager(props);

		Thread.sleep(1500);
		mgr.terminateScalar();
		mgr.waitUntilFinished();
	}

	@Test(timeout=7000)
	public void shutdownBehavior2Master() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("user_peak_load", "100");
		props.put("user_peak_duration", "100");
		props.put("scalar_minimal_cluster_size", "2");
		props.put("internal_debug_enabled", "true");

		props.put("debug_filename", "junit_thread_dump1.txt");
		Manager mgr = new Manager(props);
		Thread.sleep(500);
		props.put("debug_filename", "junit_thread_dump2.txt");
		Manager slave = new Manager(props);

		Thread.sleep(1500);
		// If manager gets it, no termination on time
		// If slave gets it (mgr == slave because of race), termination on time.
		mgr.terminateScalar();
		mgr.waitUntilFinished();
		slave.waitUntilFinished();
	}

	@Test(timeout=7000)
	public void shutdownBehavior2Slave() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("user_peak_load", "100");
		props.put("user_peak_duration", "100");
		props.put("scalar_minimal_cluster_size", "2");
		props.put("internal_debug_enabled", "true");

		props.put("debug_filename", "junit_thread_dump1.txt");
		Manager mgr = new Manager(props);
		Thread.sleep(500);
		props.put("debug_filename", "junit_thread_dump2.txt");
		Manager slave = new Manager(props);

		Thread.sleep(1500);
		// If manager gets it, no termination on time
		// If slave gets it (mgr == slave because of race), termination on time.
		slave.terminateScalar();
		mgr.waitUntilFinished();
		slave.waitUntilFinished();
	}

	@Test(timeout=10000)
	public void shutdownBehavior3() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("user_peak_load", "100");
		props.put("user_peak_duration", "100");
		props.put("scalar_minimal_cluster_size", "3");
		props.put("internal_debug_enabled", "true");

		props.put("debug_filename", "junit_thread_dump1.txt");
		Manager mgr = new Manager(props);
		props.put("debug_filename", "junit_thread_dump2.txt");
		Manager slave = new Manager(props);
		props.put("debug_filename", "junit_thread_dump3.txt");
		Manager slave2 = new Manager(props);

		Thread.sleep(1500);
		slave.terminateScalar();
		mgr.waitUntilFinished();
		slave.waitUntilFinished();
		slave2.waitUntilFinished();
	}

	@Test(timeout=7000)
	public void shutdownBehavior4NoCluster() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("user_peak_load", "100");
		props.put("user_peak_duration", "100");
		props.put("scalar_minimal_cluster_size", "2");
		props.put("internal_debug_enabled", "true");

		props.put("debug_filename", "junit_thread_dump1.txt");
		Manager mgr = new Manager(props);

		Thread.sleep(1500);
		mgr.terminateScalar();
		mgr.waitUntilFinished();
	}

	@Test(timeout=4000)
	public void watchdogTimeoutTest() throws ScalarInstantiationException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("user_peak_load", "100");
		props.put("user_peak_duration", "100");
		props.put("scalar_minimal_cluster_size", "2");
		props.put("internal_timeout_period", 1);

		Manager mgr = new Manager(props);
		mgr.waitUntilFinished();
	}
}
