package be.kuleuven.distrinet.scalar.data;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import be.kuleuven.distrinet.scalar.config.Option;
import be.kuleuven.distrinet.scalar.core.LifeCyclePreparingExperimentState;
import be.kuleuven.distrinet.scalar.core.Manager;
import be.kuleuven.distrinet.scalar.data.DataProvider;
import be.kuleuven.distrinet.scalar.exceptions.DataException;
import be.kuleuven.distrinet.scalar.exceptions.ScalarExecutionException;
import be.kuleuven.distrinet.scalar.exceptions.ScalarInstantiationException;
import be.kuleuven.distrinet.scalar.testing.TestUtils;
import be.kuleuven.distrinet.scalar.testing.TimingTester;

public class DataProviderTest {

	@Before
	public void init() {
		TestingDataProviderConnector.instance().reset();
		TimingTester.init();
	}
	
	@Test
	public void testDataProviderChain() throws ScalarInstantiationException, ScalarExecutionException, DataException {
		Properties props = TestUtils.getExperimentTestConfig();
		props.put("internal_data_providers", "LocalStorageProvider");
		
		Manager mgr = new Manager(props);
		
		mgr.waitForLifeCycleState(LifeCyclePreparingExperimentState.class);
		
		mgr.data().put("local:test123", "test");
		mgr.data().put("test123", "test");
		
		DataProvider data = mgr.data();
		assertTrue(data instanceof LocalStorageProvider);
		
		assertTrue(mgr.data().dataExists("local:test123"));
		assertTrue(mgr.data().dataExists("test123"));
		assertFalse(mgr.data().dataExists("local:test12345"));
	}
	
	@Test
	public void testLocalDataProvider() throws ScalarInstantiationException, ScalarExecutionException, DataException, InterruptedException {
		Properties props = TestUtils.getExperimentTestConfig();
		props.put("internal_data_providers", "LocalStorageProvider");
		
		Manager mgr = new Manager(props);
		
		mgr.waitForLifeCycleState(LifeCyclePreparingExperimentState.class);
		
		mgr.data().put("local:test123", "test");
		mgr.data().put("test123", "test");
		
		assertTrue(mgr.data().dataExists("local:test123"));
		assertTrue(mgr.data().dataExists("test123"));
		assertFalse(mgr.data().dataExists("local:test12345"));
	}
	
	@Test
	public void testHazelcastProvider() throws ScalarInstantiationException, ScalarExecutionException, DataException, InterruptedException {
		Properties props = TestUtils.getExperimentTestConfig();
		props.put("internal_data_providers", "LocalStorageProvider,HazelcastProvider");
		
		Manager mgr = new Manager(props);
		
		mgr.waitForLifeCycleState(LifeCyclePreparingExperimentState.class);
		
		mgr.data().put("local:test123", "test");
		mgr.data().put("test123", "test");
		
		assertTrue(mgr.data().dataExists("local:test123"));
		assertTrue(mgr.data().dataExists("test123"));
		assertFalse(mgr.data().dataExists("local:test12345"));
	}
	
	@Test
	public void testIgniteProvider() throws ScalarInstantiationException, ScalarExecutionException, DataException, InterruptedException {
		Properties props = TestUtils.getExperimentTestConfig();
		props.put("internal_data_providers", "LocalStorageProvider,IgniteProvider");
		
		Manager mgr = new Manager(props);
		
		mgr.waitForLifeCycleState(LifeCyclePreparingExperimentState.class);
		
		mgr.data().put("local:test123", "test");
		mgr.data().put("test123", "test");
		
		assertTrue(mgr.data().dataExists("local:test123"));
		assertTrue(mgr.data().dataExists("test123"));
		assertFalse(mgr.data().dataExists("local:test12345"));
	}
	
	@Test
	public void testDistributedDataProvider() throws ScalarInstantiationException, ScalarExecutionException, DataException, InterruptedException {
		Properties props = TestUtils.getExperimentTestConfig();
		props.put("scalar_minimal_cluster_size", 2);
		
		Manager scalar1 = new Manager(props);
		//scalar1.beginManaging();
		Manager scalar2 = new Manager(props);
		
		scalar1.waitForLifeCycleState(LifeCyclePreparingExperimentState.class);
		
		scalar1.data().put("local:test123", "test");
		scalar1.data().put("test123", "test");
		
		scalar2.waitForLifeCycleState(LifeCyclePreparingExperimentState.class);
		
		assertTrue(scalar1.data().dataExists("local:test123"));
		assertFalse(scalar2.data().dataExists("local:test123"));
		assertTrue(scalar1.data().dataExists("test123"));
		assertTrue(scalar2.data().dataExists("test123"));
		assertFalse(scalar1.data().dataExists("test12345"));
	}
	
	@Test
	public void testConfigurationBackup() throws ScalarInstantiationException, ScalarExecutionException, DataException, InterruptedException {
		Properties props = TestUtils.getMinimalTestConfig();
		props.put("internal_data_providers", "TestingDataProvider");
		props.put("testvalue", "test1");
				
		Manager mgr = new Manager(props);
		
		mgr.waitForLifeCycleState(LifeCyclePreparingExperimentState.class);
		
		assertTrue(mgr.data().get("testvalue").equals("test1"));
		mgr.data().put("testvalue", "test2");
		
		assertFalse(mgr.data().get("testvalue").equals("test1"));
		
		Map<String,Object> backup = (Map<String, Object>) mgr.data().get(Option.CONFIGURATION_BACKUP.toString());
		assertTrue(backup.get("testvalue").equals("test1"));
	}
	
}
