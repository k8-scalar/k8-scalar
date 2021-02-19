package be.kuleuven.distrinet.scalar.core;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

import be.kuleuven.distrinet.scalar.exceptions.DataException;
import be.kuleuven.distrinet.scalar.exceptions.ScalarExecutionException;
import be.kuleuven.distrinet.scalar.exceptions.ScalarInstantiationException;
import be.kuleuven.distrinet.scalar.testing.EvilTester;
import be.kuleuven.distrinet.scalar.testing.TestUtils;
import be.kuleuven.distrinet.scalar.testing.TimingTester;

public class UserTest {

	//@Test
	public void socketUserTest() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("user_peak_load", "1");
		props.put("think_time", "1000");
		props.put("user_peak_duration", "1000");
		props.put("user_implementations", "be.kuleuven.distrinet.scalar.users.SocketUser:1");
		props.put("think_time_strategy_factory", "ExponentialThinkTimeStrategyFactory");
		props.put("plugins", props.get("plugins") + ",be.kuleuven.distrinet.scalar.testing.TimingTestPlugin,be.kuleuven.distrinet.scalar.plugin.SocketUserHelperPlugin");
		
		TimingTester.init();
		
		Manager instance = new Manager(props);
		instance.waitUntilFinished();
		
		assertTrue(TimingTester.instance().checkAllPhasesDone());
		assertEquals(TimingTester.instance().getTotalRequestCount(), 20, 1);
		assertEquals(TimingTester.instance().getNbRunsPerformed(), 1);
	}
	
	@Test
	public void evilUserTest() throws ScalarInstantiationException, ScalarExecutionException, InterruptedException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("user_peak_load", "10");
		props.put("think_time", "1000");
		props.put("user_peak_duration", "2");
		props.put("user_implementations", "be.kuleuven.distrinet.scalar.testing.EvilTester:1");
		props.put("think_time_strategy_factory", "ExponentialThinkTimeStrategyFactory");
		
		EvilTester.setFailureProbability(0.5);
		
		Manager instance = new Manager(props);
		instance.waitUntilFinished();
		
		assertTrue(instance.finishedSuccessfully());
	}
	
}
