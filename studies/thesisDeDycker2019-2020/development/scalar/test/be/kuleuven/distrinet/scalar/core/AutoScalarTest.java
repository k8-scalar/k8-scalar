package be.kuleuven.distrinet.scalar.core;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.distrinet.scalar.config.Option;
import be.kuleuven.distrinet.scalar.data.TestingDataProvider;
import be.kuleuven.distrinet.scalar.data.TestingDataProviderConnector;
import be.kuleuven.distrinet.scalar.exceptions.DataException;
import be.kuleuven.distrinet.scalar.exceptions.ScalarExecutionException;
import be.kuleuven.distrinet.scalar.exceptions.ScalarInstantiationException;
import be.kuleuven.distrinet.scalar.experiment.Experiment;
import be.kuleuven.distrinet.scalar.testing.TestUtils;
import be.kuleuven.distrinet.scalar.testing.TimingTester;

public class AutoScalarTest {

	@Before
	public void init() {
		TestingDataProviderConnector.instance().reset();
	}
	
	@Test
	public void singleInstanceTest() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Properties props = TestUtils.getAutoScalarTestConfig();
		props.put("think_time_strategy_factory", "ExponentialThinkTimeStrategyFactory");
		props.put("user_implementations", "be.kuleuven.distrinet.scalar.testing.TimingTestUser:1");
		props.put("think_time_strategy_factory", "ExponentialThinkTimeStrategyFactory");
		props.put("user_warmup_duration", "1");
		props.put("user_peak_duration", "5");
		props.put("user_wait_inbetween_runs", "0");
				
		TimingTester.init(100, 1.0);
		
		Manager instance = new Manager(props);
		instance.waitUntilFinished();
		
		assertTrue(TimingTester.instance().checkAllPhasesDone());
	}
	
	// @Test
	public void autoClusterTest() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException, DataException {
		Properties props = TestUtils.getAutoScalarTestConfig();
		props.put("scalar_minimal_cluster_size", "2");
		props.put("user_warmup_duration", "0");
		props.put("user_warmup_fraction", "1.0");
		props.put("user_peak_duration", "1");
		props.put("user_wait_inbetween_runs", "0");
				
		TimingTester.init(100, 1.0);
		
		Manager master = new Manager(props);
		assertTrue(master.isMaster());
		
		assertTrue(TestingDataProviderConnector.instance().primary() != null);
		assertFalse((boolean)TestingDataProviderConnector.instance().primary().get(Option.RUN_LOCKED.toString()));
		assertFalse(master.finished());
		
		Manager slave = new Manager(props);
		assertFalse(slave.isMaster());
		
		master.waitUntilFinished();
		slave.waitUntilFinished();
		
		assertTrue(master.finished());
		assertTrue(slave.finished());
		
		int nbRuns = ((Experiment)TestingDataProviderConnector.instance().primary().get(Option.RUN_EXPERIMENT.toString())).nbRuns();
		// int nbCalibratedRuns = Integer.parseInt(TestingDataProvider._defaultConfig.get(Option.INTERNAL_NB_CALIBRATED_RUNS.toString()));
		// assertTrue("Number of runs is " + nbRuns + ", number of calibrated runs should have been " + nbCalibratedRuns, nbRuns > nbCalibratedRuns);

		assertTrue(TimingTester.instance().checkAllPhasesDone());
		
		// XXX resultaten trekken op niets
	}
	
	@Test
	public void autoRebalancingSimpleTest() throws ScalarInstantiationException, ScalarExecutionException, DataException, InterruptedException {
		Properties props = TestUtils.getAutoScalarTestConfig();
		props.put("scalar_minimal_cluster_size", "3");
		props.put("plugins", props.get("plugins") + ",be.kuleuven.distrinet.scalar.plugin.ClusterMonitor");
		props.put("user_warmup_duration", "0");
		props.put("user_warmup_fraction", "1.0");
		props.put("user_peak_duration", "1");
		props.put("user_wait_inbetween_runs", "0");
		props.put("mode", "manual");
		props.put("user_peak_load", "100");
				
		TimingTester.init(100, 1.0);
		
		Manager master = new Manager(props);
		Manager slave1 = new Manager(props);
		Manager slave2 = new Manager(props);
		
		ArrayList<UUID> cluster = new ArrayList<UUID>();
		HashMap<UUID,Double> balance = new HashMap<UUID,Double>();
		
		cluster.add(master.localID());
		balance.put(master.localID(), 0.5);
		
		cluster.add(slave1.localID());
		balance.put(slave1.localID(), 0.25);
		
		cluster.add(slave2.localID());
		balance.put(slave2.localID(), 0.25);
		
		TestingDataProviderConnector.instance().primary().put(Option.RUN_CALIBRATION.toString(), balance);
		
		master.waitUntilFinished();
		slave1.waitUntilFinished();
		slave2.waitUntilFinished();

		assertTrue((boolean)TestingDataProviderConnector.instance().primary().get(Option.EXPERIMENT_STATUS_OK.toString()));
	}
	
	@Test
	public void autoRebalancingRoundingTest() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException, DataException {
		Properties props = TestUtils.getAutoScalarTestConfig();
		props.put("scalar_minimal_cluster_size", "3");
		props.put("plugins", props.get("plugins") + ",be.kuleuven.distrinet.scalar.plugin.ClusterMonitor");
		props.put("user_warmup_duration", "0");
		props.put("user_warmup_fraction", "1.0");
		props.put("user_peak_duration", "1");
		props.put("user_wait_inbetween_runs", "0");
		props.put("mode", "manual");
		props.put("user_peak_load", "101");
				
		TimingTester.init(100, 1.0);
		
		Manager master = new Manager(props);
		Manager slave1 = new Manager(props);
		Manager slave2 = new Manager(props);
		
		ArrayList<UUID> cluster = new ArrayList<UUID>();
		HashMap<UUID,Double> balance = new HashMap<UUID,Double>();
		
		cluster.add(master.localID());
		balance.put(master.localID(), 1.92);
		
		cluster.add(slave1.localID());
		balance.put(slave1.localID(), 0.13);
		
		cluster.add(slave2.localID());
		balance.put(slave2.localID(), 0.73);
		
		TestingDataProviderConnector.instance().primary().put(Option.RUN_CALIBRATION.toString(), balance);
		
		master.waitUntilFinished();
		slave1.waitUntilFinished();
		slave2.waitUntilFinished();
		
		assertTrue((boolean)TestingDataProviderConnector.instance().primary().get(Option.EXPERIMENT_STATUS_OK.toString()));
	}
	
}
