package be.kuleuven.distrinet.scalar.testing;

import be.kuleuven.distrinet.scalar.core.User;
import be.kuleuven.distrinet.scalar.core.UserPool;
import be.kuleuven.distrinet.scalar.exceptions.DataException;
import be.kuleuven.distrinet.scalar.exceptions.RequestException;

public class TimingTestUser extends User {

	private TimingTester _tester;
	
	public TimingTestUser(UserPool pool) {
		super(pool);
		_tester = TimingTester.instance();
		_tester.newUser(this);
	}

	@Override
	public void mainLoop() throws DataException, RequestException {
		TimingTestRequest req = new TimingTestRequest(this);
		req.doRequest(_tester);
	}

}
