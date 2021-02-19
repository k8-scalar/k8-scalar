package be.kuleuven.distrinet.scalar.core;

import static org.junit.Assert.*;

import java.util.Properties;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import be.kuleuven.distrinet.scalar.data.TestingDataProvider;
import be.kuleuven.distrinet.scalar.data.TestingDataProviderConnector;
import be.kuleuven.distrinet.scalar.exceptions.DataException;
import be.kuleuven.distrinet.scalar.exceptions.ScalarExecutionException;
import be.kuleuven.distrinet.scalar.exceptions.ScalarInstantiationException;
import be.kuleuven.distrinet.scalar.testing.TestUtils;
import be.kuleuven.distrinet.scalar.testing.TimingTester;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExperimentTest {

	@Before
	public void init() {
		TestingDataProviderConnector.instance().reset();
	}
	
	@Test
	public void allPhasesExecuted() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("user_peak_load", "0");
		props.put("think_time", "1000");
		props.put("user_peak_duration", "1");
		props.put("think_time_strategy_factory", "ExponentialThinkTimeStrategyFactory");
		props.put("plugins", props.get("plugins") + ",be.kuleuven.distrinet.scalar.testing.TimingTestPlugin");
		
		TimingTester.init();
		
		performExperiment(props);
		
		assertTrue(TimingTester.instance().checkAllPhasesDone());
	}
	
	@Test
	public void constantRequestRate() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("user_peak_load", "1");
		props.put("think_time", "100");
		props.put("user_peak_duration", "5");
		props.put("user_warmup_duration", "0");
		props.put("user_ramp_up_duration", "0");
		props.put("user_ramp_down_duration", "0");
		props.put("user_cooldown_duration", "0");
		props.put("user_warmup_fraction", "1.0");
		props.put("user_implementations", "be.kuleuven.distrinet.scalar.testing.TimingTestUser:1");
		props.put("think_time_strategy_factory", "ConstantThinkTimeStrategyFactory");
		
		TimingTester.init();
		
		performExperiment(props);
		
		DescriptiveStatistics stats = TimingTester.instance().getInterArrivalStats();
		assertEquals(50, TimingTester.instance().getTotalRequestCount(), 2);
		assertTrue(TimingTester.instance().getTotalRequestCount() <= 50);
		assertEquals(100, stats.getMean(), 5);
		assertEquals(0, stats.getStandardDeviation(), 3);
	}
	
	@Test
	public void exponentialRequestRate() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("user_peak_load", "1");
		props.put("think_time", "20");
		props.put("user_peak_duration", "5");
		props.put("user_warmup_duration", "0");
		props.put("user_ramp_up_duration", "0");
		props.put("user_ramp_down_duration", "0");
		props.put("user_cooldown_duration", "0");
		props.put("user_warmup_fraction", "1.0");
		props.put("user_implementations", "be.kuleuven.distrinet.scalar.testing.TimingTestUser:1");
		props.put("think_time_strategy_factory", "ExponentialThinkTimeStrategyFactory");
		
		TimingTester.init();
		
		performExperiment(props);
		
		DescriptiveStatistics stats = TimingTester.instance().getInterArrivalStats();
		assertEquals(20, stats.getMean(), 5);
		assertEquals(20, stats.getStandardDeviation(), 5);
	}
	
	@Test
	public void sigmaRun() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Properties props = TestUtils.getExperimentTestConfig();
		props.put("think_time_strategy_factory", "ConstantThinkTimeStrategyFactory");
		
		TimingTester.init(200,0);
		
		performExperiment(props);
		
		assertEquals(5e-03, TimingTester.instance().getSigma(), 1e-02);
		assertEquals(5e-05, TimingTester.instance().getKappa(), 1e-04);
	}
	
	@Test
	public void kappaRun() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Properties props = TestUtils.getExperimentTestConfig();
		
		TimingTester.init(20,0.2);
		
		performExperiment(props);
		
		assertEquals(5e-08, TimingTester.instance().getSigma(), 1e-07);
		assertEquals(5e-03, TimingTester.instance().getKappa(), 1e-02);
	}
	
	@Test
	public void prestartUsers() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Properties props = TestUtils.getExperimentTestConfig();
		props.put("user_peak_load", "1");
		props.put("user_implementations", "be.kuleuven.distrinet.scalar.users.TestUser:1");
		props.put("user_implementations_prestart", "be.kuleuven.distrinet.scalar.testing.TimingTestUser:1");
		props.put("think_time_strategy_factory", "ConstantThinkTimeStrategyFactory");
		props.put("user_warmup_duration", "0");
		props.put("think_time", "1000");
		props.put("user_peak_duration", "5");
		
		TimingTester.init();
		
		performExperiment(props);
		
		assertEquals(5, TimingTester.instance().getTotalRequestCount(), 1);
	}

	private void performExperiment(Properties props)
			throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Manager instance = new Manager(props);
		instance.waitUntilFinished();
		assertTrue(instance.finishedSuccessfully());
	}
}
