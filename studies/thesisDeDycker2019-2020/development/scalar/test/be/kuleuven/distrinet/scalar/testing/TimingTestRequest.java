package be.kuleuven.distrinet.scalar.testing;

import be.kuleuven.distrinet.scalar.core.User;
import be.kuleuven.distrinet.scalar.exceptions.InternalException;
import be.kuleuven.distrinet.scalar.exceptions.RequestException;
import be.kuleuven.distrinet.scalar.requests.Request;
import be.kuleuven.distrinet.scalar.requests.RequestResult;

public class TimingTestRequest extends Request {

	public TimingTestRequest(User usr) {
		super(usr, true);
	}

	public void doRequest(TimingTester tester) {
		startTimer();
		tester.request();
		stopTimer();
		try {
			done(RequestResult.SUCCEEDED);
		} catch (RequestException e) {
			throw new InternalException(e);
		}
	}

}
