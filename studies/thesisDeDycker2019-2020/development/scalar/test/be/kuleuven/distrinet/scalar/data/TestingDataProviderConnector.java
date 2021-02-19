package be.kuleuven.distrinet.scalar.data;

import be.kuleuven.distrinet.scalar.core.Manager;
import be.kuleuven.distrinet.scalar.exceptions.DataException;

public class TestingDataProviderConnector {

	private static TestingDataProviderConnector _instance = new TestingDataProviderConnector();

	public static TestingDataProviderConnector instance() {
		return _instance;
	}

	private TestingDataProvider _primary;
	private int _actualConnections;

	private TestingDataProviderConnector() {
		_actualConnections = 0;
		_primary = null;
	}

	public void connect(TestingDataProvider provider) {
		synchronized(this) {
			if (_primary == null) {
				_primary = provider;
			} else {
				provider.setAsPrimary(_primary);
			}
		}

		_actualConnections++;
	}

	public Manager primaryManager() {
		return _primary.manager();
	}
	
	public void reset() {
		_primary = null;
		_actualConnections = 0;
	}

	public TestingDataProvider primary() {
		synchronized (this) {
			return _primary;	
		}
	}

}
