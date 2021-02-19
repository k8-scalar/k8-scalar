package be.kuleuven.distrinet.scalar.testing;

import java.util.ArrayList;

import be.kuleuven.distrinet.scalar.core.Plugin;
import be.kuleuven.distrinet.scalar.core.User;
import be.kuleuven.distrinet.scalar.exceptions.DataException;
import be.kuleuven.distrinet.scalar.exceptions.InternalException;
import be.kuleuven.distrinet.scalar.requests.Request;

public class UserStopTestPlugin extends Plugin {

	@Override
	protected void onRequest(Request r) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onStartUp() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onWarmUp() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onRampUp() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onPeak() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onRampDown() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onCoolDown() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onStop(ArrayList<Request> allRequests) {
		try {
			data().put("testing-successful", true);

			for (User u : users()) {
				if (u instanceof StopTestUser) {
					if (((StopTestUser) u).isRunning()) {
						data().put("testing-successful", false);
					}
				}
			}
		} catch (DataException e) {
			throw new InternalException(e);
		}
	}

	@Override
	protected void onTermination() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onInitialization() {
		// TODO Auto-generated method stub

	}

}
