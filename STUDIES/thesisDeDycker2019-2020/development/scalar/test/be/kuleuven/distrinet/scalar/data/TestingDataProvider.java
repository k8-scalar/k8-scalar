package be.kuleuven.distrinet.scalar.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import be.kuleuven.distrinet.scalar.config.DefaultConfiguration;
import be.kuleuven.distrinet.scalar.core.Manager;
import be.kuleuven.distrinet.scalar.data.DataProvider;
import be.kuleuven.distrinet.scalar.exceptions.DataException;
import be.kuleuven.distrinet.scalar.exceptions.LockException;

public class TestingDataProvider extends LocalStorageProvider {

	private static HashMap<String,Integer> _errorOnKeys;

	private static double _errorProbability;

	public static void triggerErrorOnFetch(HashMap<String,Integer> keys) {
		_errorOnKeys = keys;
	}
	
	public static void triggerRandomErrors(double probability) {
		_errorProbability = probability;
	}

	private Random _rnd;

	private HashMap<String, Integer> _accesses;
	
	synchronized public void setAsPrimary(TestingDataProvider primary) {
		if (! locks().isEmpty() || ! latches().isEmpty() || ! data().isEmpty()) {
			throw new RuntimeException("WARNING!!! " + this + " connecting to primary data provider " + primary +
					" while not empty!");
		}
		
		setLocks(primary.locks());
		setLatches(primary.latches());
		setData(primary.data());
	}
	
	public TestingDataProvider(Manager mgr) {
		super(mgr);
		
		_rnd = new Random();
		_accesses = new HashMap<String,Integer>();
		
		TestingDataProviderConnector.instance().connect(this);
	}

	@Override
	protected Object getValue(String key) throws DataException {
		if (_accesses.containsKey(key)) {
			_accesses.put(key, _accesses.get(key) + 1);
		} else {
			_accesses.put(key, 1);
		}
		
		if (_errorOnKeys != null && _errorOnKeys.containsKey(key) && _errorOnKeys.get(key) >= _accesses.get(key)) {
			throw new DataException("Simulated error on fetch of " + key + ".");
		}
		
		if (_errorProbability != 0 && _rnd.nextDouble() < _errorProbability) {
			throw new DataException("Simulated random error on fetch of " + key + ".");
		}
		
		if (data().containsKey(key)) {
			return super.getValue(key);
		}
		
		return null;
	}

}
