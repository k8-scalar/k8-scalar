package be.kuleuven.distrinet.scalar.requests;

import be.kuleuven.distrinet.scalar.core.User;
import be.kuleuven.distrinet.scalar.exceptions.RequestException;
import be.kuleuven.distrinet.scalar.reporting.Report;

public class TestRequest extends Request {

	public TestRequest(User usr) {
		super(usr, true);
	}
	
	public void doRequest() throws RequestException {
		startTimer();
		// ...
		// SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		// String result = dateFormat.format(System.currentTimeMillis());
		// System.out.println(result + " - " + user().username() + " request");
		stopTimer();
		done(RequestResult.SUCCEEDED);
	}
	
}
