package be.kuleuven.distrinet.scalar.junit;

import java.util.Arrays;

import org.junit.Ignore;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import be.kuleuven.distrinet.scalar.data.TestingDataProvider;

public class FailureTestRunner extends BlockJUnit4ClassRunner {

	public FailureTestRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier) {
		Description description = describeChild(method);
		if (method.getAnnotation(Ignore.class) != null) {
			notifier.fireTestIgnored(description);
		} else {
			runWithFailureConditions(0.1, method, notifier, description);
			runWithFailureConditions(0.01, method, notifier, description);
			runWithFailureConditions(0.001, method, notifier, description);
			runWithFailureConditions(0.0001, method, notifier, description);
			runWithFailureConditions(0, method, notifier, description);
		}
	}

	private void runWithFailureConditions(double dataFailProbability, FrameworkMethod method, RunNotifier notifier, Description description) {
		System.out.println(">>> Running " + method.getDeclaringClass().getSimpleName() + "." + 
				method.getName() + " with data failure probability " + dataFailProbability + "...");
		TestingDataProvider.triggerRandomErrors(dataFailProbability);
		RunRules runRules = new RunRules(methodBlock(method), Arrays.asList(new TestRule[]{
				Timeout.seconds(60),
				new FailureHandlingTestRule()
		}), description);
		runLeaf(runRules, description, notifier);
	}

}
