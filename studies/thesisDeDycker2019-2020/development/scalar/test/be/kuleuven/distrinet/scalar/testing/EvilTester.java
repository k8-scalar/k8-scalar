package be.kuleuven.distrinet.scalar.testing;

import be.kuleuven.distrinet.scalar.core.User;
import be.kuleuven.distrinet.scalar.core.UserPool;
import be.kuleuven.distrinet.scalar.exceptions.DataException;
import be.kuleuven.distrinet.scalar.exceptions.RequestException;

public class EvilTester extends User {

	private static double _failureProbability;
	
	public static void setFailureProbability(double prob) {
		_failureProbability = prob;
	}
	
	public EvilTester(UserPool pool) {
		super(pool);
	}
	
	@Override
	public void mainLoop() throws DataException, RequestException {
		if (random().nextDouble() < _failureProbability) {
			throw new TestingException();
		}
	}

}
