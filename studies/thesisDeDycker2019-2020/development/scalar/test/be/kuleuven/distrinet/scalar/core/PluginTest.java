package be.kuleuven.distrinet.scalar.core;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.distrinet.scalar.data.TestingDataProvider;
import be.kuleuven.distrinet.scalar.data.TestingDataProviderConnector;
import be.kuleuven.distrinet.scalar.exceptions.DataException;
import be.kuleuven.distrinet.scalar.exceptions.ScalarExecutionException;
import be.kuleuven.distrinet.scalar.exceptions.ScalarInstantiationException;
import be.kuleuven.distrinet.scalar.testing.EvilPlugin;
import be.kuleuven.distrinet.scalar.testing.TestUtils;

public class PluginTest {

	@Before
	public void init() {
		TestingDataProviderConnector.instance().reset();
	}
	
	@Test
	public void testFeatureModelParser() throws ScalarInstantiationException, ScalarExecutionException, DataException, InterruptedException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("feature_model_mapping", "test/be/kuleuven/distrinet/scalar/testing/test-feature-mapping.conf");
		props.put("feature_model_config", "TestFeature1,TestFeature3");
		props.put("plugins", props.get("plugins") + ",be.kuleuven.distrinet.scalar.plugin.FeatureModelParser");
				
		Manager instance = new Manager(props);
		instance.waitUntilFinished();
		
		assertNotNull(TestingDataProviderConnector.instance().primary().get("testing:TestFeatureHelper"));
		assertTrue(TestingDataProviderConnector.instance().primary().get("testing:TestFeatureHelper").equals("succeeded"));
	}
	
	@Test
	public void testEvilPlugins() throws ScalarInstantiationException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("user_peak_duration", "3");
		props.put("user_peak_load", "1000");
		props.put("plugins", "be.kuleuven.distrinet.scalar.testing.EvilPlugin," + props.get("plugins"));
		
		EvilPlugin.setFailureProbability(0.1);
		
		Manager instance = new Manager(props);
		instance.waitUntilFinished();
		
		assertTrue(instance.finished());
	}
}
