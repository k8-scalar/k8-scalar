package be.kuleuven.distrinet.scalar;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import be.kuleuven.distrinet.scalar.core.AutoScalarTest;
import be.kuleuven.distrinet.scalar.core.ExperimentTest;
import be.kuleuven.distrinet.scalar.core.ManagerTest;
import be.kuleuven.distrinet.scalar.core.PluginTest;
import be.kuleuven.distrinet.scalar.core.UserTest;
import be.kuleuven.distrinet.scalar.data.DataProviderTest;
import be.kuleuven.distrinet.scalar.junit.ScalarSuite;
import be.kuleuven.distrinet.scalar.reporting.Report;

@RunWith(Suite.class)
@SuiteClasses({
	ManagerTest.class, 
	UserTest.class,
	PluginTest.class,
	DataProviderTest.class,
	ExperimentTest.class,
//	AutoScalarTest.class
	})
public class FunctionalTestSuite {

	@BeforeClass
	public static void setUp() {
		Report.disableConsoleOutput();
	}

	@AfterClass
	public static void tearDown() {
		
	}

}
