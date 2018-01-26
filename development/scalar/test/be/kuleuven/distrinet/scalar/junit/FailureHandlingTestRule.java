package be.kuleuven.distrinet.scalar.junit;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestTimedOutException;

import be.kuleuven.distrinet.scalar.config.Option;
import be.kuleuven.distrinet.scalar.core.DebugHelper;
import be.kuleuven.distrinet.scalar.core.Manager;
import be.kuleuven.distrinet.scalar.data.TestingDataProvider;
import be.kuleuven.distrinet.scalar.data.TestingDataProviderConnector;
import be.kuleuven.distrinet.scalar.exceptions.DataException;
import be.kuleuven.distrinet.scalar.exceptions.ScalarExecutionException;
import be.kuleuven.distrinet.scalar.exceptions.ScalarInstantiationException;
import be.kuleuven.distrinet.scalar.reporting.Report;

public class FailureHandlingTestRule implements TestRule {
	
	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				try {
					base.evaluate();
				} catch (AssertionError t) {
					String msg = "";
					if (t.getLocalizedMessage() != null) {
						msg = " (" + t.getLocalizedMessage() + ")";
					}
					System.out.println("\tFailed assertion" + msg + " - OK");
				} catch (TestTimedOutException t) {
					// Deadlock detected!
					System.out.println();
					System.out.println("!!! Possible deadlock detected !!!");
					System.out.println("Stack dump written to " +
							doStackDump(TestingDataProviderConnector.instance().primary(), description.getMethodName())
							+ ".");
					System.out.println();
					throw t;
				} catch (ScalarInstantiationException | ScalarExecutionException t) {
					String msg = "";
					if (t.getLocalizedMessage() != null) {
						msg = " (" + t.getLocalizedMessage() + ")";
					}
					System.out.println("\tCaught Scalar exception - " + msg + " - OK");
				} catch (Throwable t) {
					String msg = "";
					if (t.getLocalizedMessage() != null) {
						msg = " (" + t.getLocalizedMessage() + ")";
					}
					System.out.println("\tUncaught exception (" + t.getClass().getSimpleName() + ") - " + msg + " - NOT OK");
					throw t;
				}
			}
		};
	}
	
	public String doStackDump(TestingDataProvider dataProvider, String methodName) {
		String debug = generateStackDump();
		
		StringBuilder data = new StringBuilder();
		data.append("--- BEGIN DATA PROVIDER DUMP ---\n");
		try {
			for (String key : dataProvider.keys()) {
				data.append(key);
				data.append(" -> ");
				data.append(dataProvider.get(key).toString());
				data.append("\n");
			}
		} catch (DataException e1) {
			data.append("!!! Exception during data gathering: " + e1.getLocalizedMessage());
		}
		data.append("--- END DATA PROVIDER DUMP ---\n");

		Report dump = new Report("debug");
		dump.put("stack", debug);
		dump.put("data", data.toString());

		String file = "junit-dump-" + methodName + "-" + System.currentTimeMillis() + ".log";
		dump.sendToFile(file);
		
		return file;
	}
	
	private String generateStackDump() {
		String stackdump = "";
		Map<Thread, StackTraceElement[]> liveThreads = Thread.getAllStackTraces();
		for (Iterator<Thread> i = liveThreads.keySet().iterator(); i.hasNext(); ) {
			Thread key = (Thread)i.next();
			stackdump += "Thread " + key.getName() + "\n";
			StackTraceElement[] trace = (StackTraceElement[])liveThreads.get(key);
			for (int j = 0; j < trace.length; j++) {
				stackdump += "\tat " + trace[j] + "\n";
			}
		}
		return stackdump;
	}
}
