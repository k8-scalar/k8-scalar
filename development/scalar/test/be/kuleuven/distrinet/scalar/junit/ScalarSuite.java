package be.kuleuven.distrinet.scalar.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class ScalarSuite extends Suite {
	
	public ScalarSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
		super(klass, getRunners(getAnnotatedClasses(klass)));
	}

	// From Suite...
	private static Class<?>[] getAnnotatedClasses(Class<?> klass) throws InitializationError {
        Suite.SuiteClasses annotation = klass.getAnnotation(Suite.SuiteClasses.class);
        if (annotation == null) {
            throw new InitializationError(String.format("class '%s' must have a SuiteClasses annotation", klass.getName()));
        }
        return annotation.value();
    }
	
	private static List<Runner> getRunners(Class<?>[] classes) throws InitializationError {
		List<Runner> runners = new ArrayList<Runner>();
		for (Class<?> klazz : classes) {
            runners.add(new FailureTestRunner(klazz));
        }
		return runners;
	}

}
