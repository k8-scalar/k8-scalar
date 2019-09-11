package be.kuleuven.distrinet.scalar.testing;

import java.util.ArrayList;
import java.util.Random;

import be.kuleuven.distrinet.scalar.core.Plugin;
import be.kuleuven.distrinet.scalar.requests.Request;

public class EvilPlugin extends Plugin {

	private static double _failureProbability;
	
	public static void setFailureProbability(double prob) {
		_failureProbability = prob;
	}
	
	private Random _random;
	
	public EvilPlugin() {
		_random = new Random();
	}
	
	@Override
	protected void onRequest(Request r) {
		if (_random.nextDouble() < _failureProbability) {
			throw new TestingException();
		}
	}

	@Override
	protected void onStartUp() {
	}

	@Override
	protected void onWarmUp() {
	}

	@Override
	protected void onRampUp() {
	}

	@Override
	protected void onPeak() {
	}

	@Override
	protected void onRampDown() {
	}

	@Override
	protected void onCoolDown() {
	}

	@Override
	protected void onStop(ArrayList<Request> allRequests) {
	}

	@Override
	protected void onTermination() {
		throw new TestingException();
	}

	@Override
	protected void onInitialization() {
	}

}
