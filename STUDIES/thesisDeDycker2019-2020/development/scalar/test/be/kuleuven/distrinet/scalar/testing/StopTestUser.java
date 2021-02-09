package be.kuleuven.distrinet.scalar.testing;

import be.kuleuven.distrinet.scalar.core.User;
import be.kuleuven.distrinet.scalar.core.UserPool;
import be.kuleuven.distrinet.scalar.exceptions.DataException;
import be.kuleuven.distrinet.scalar.exceptions.RequestException;

public class StopTestUser extends User {

	private boolean _running;

	public StopTestUser(UserPool pool) {
		super(pool);
		_running = false;
	}

	@Override
	public void mainLoop() throws DataException, RequestException {
		_running = true;
		doCalculation();
		_running = false;
	}

	public boolean isRunning() {
		return _running;
	}
	
	private void doCalculation() {
		nthPrime(500);
	}

	public int nthPrime(int n) {
	    int candidate, count;
	    for(candidate = 2, count = 0; count < n; ++candidate) {
	        if (isPrime(candidate)) {
	            ++count;
	        }
	    }
	    // The candidate has been incremented once after the count reached n
	    return candidate-1;
	}
	
	private boolean isPrime(int n) {
	    for(int i = 2; i < n; ++i) {
	        if (n % i == 0) {
	            // We are naive, but not stupid, if
	            // the number has a divisor other
	            // than 1 or itself, we return immediately.
	            return false;
	        }
	    }
	    return true;
	}
}
