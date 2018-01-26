package be.kuleuven.distrinet.scalar;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import be.kuleuven.distrinet.scalar.core.ExperimentTest;
import be.kuleuven.distrinet.scalar.core.ManagerTest;
import be.kuleuven.distrinet.scalar.core.PluginTest;
import be.kuleuven.distrinet.scalar.data.DataProviderTest;
import be.kuleuven.distrinet.scalar.junit.ScalarSuite;
import be.kuleuven.distrinet.scalar.reporting.Report;

@RunWith(ScalarSuite.class)
@SuiteClasses({
	ManagerTest.class, 
//	UserTest.class,
	PluginTest.class,
	DataProviderTest.class,
	ExperimentTest.class,
//	AutoScalarTest.class
	})
public class FailureHandlingTestSuite {
	
	@BeforeClass
	public static void setUp() {
		Report.disableConsoleOutput();
	}

}
