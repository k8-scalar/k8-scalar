package be.kuleuven.distrinet.scalar.testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import be.kuleuven.distrinet.scalar.core.Clock;
import be.kuleuven.distrinet.scalar.experiment.Run;
import be.kuleuven.distrinet.scalar.util.Pair;

public class TimingTester {

	private static TimingTester _instance;

	private List<Pair<UUID,Run>> _runs;
	
	private HashMap<Pair<UUID,Run>, Boolean> _runSuccessful;
	
	private static ReadWriteLock _lock;
	
	public static TimingTester instance() {
		return _instance;
	}
	
	public static void init() {
		_instance = new TimingTester();
	}
	
	public static void init(int workload, double locking) {
		_instance = new TimingTester(workload, locking);
	}

	private double _locking;
	private int _workload;
	
	public TimingTester() {
		this(0, 0);
	}
	
	private List<TimingTestUser> _users;
	
	public void newUser(TimingTestUser u) {
		_users.add(u);
	}
	
	public List<TimingTestUser> users() {
		return _users;
	}
	
	public TimingTester(int workload, double locking) {
		_users = (List<TimingTestUser>) Collections.synchronizedList(new ArrayList<TimingTestUser>());
		_timestamps = new ArrayList<Long>();
		_timestamps = Collections.synchronizedList(_timestamps);
		_runs = new ArrayList<Pair<UUID,Run>>();
		_runSuccessful = new HashMap<Pair<UUID,Run>, Boolean>();
		_lock = new ReentrantReadWriteLock();
		_workload = workload;
		_locking = locking;
		initDone = new HashSet<UUID>();
		startDone = new HashSet<UUID>();
		rampUpDone = new HashSet<UUID>();
		peakDone = new HashSet<UUID>();
		rampDownDone = new HashSet<UUID>();
		coolDownDone = new HashSet<UUID>();
		terminateDone = new HashSet<UUID>();
		warmupDone = new HashSet<UUID>();
	}
	
	private List<Long> _timestamps;
	
	private HashSet<UUID> initDone;
	private HashSet<UUID> startDone;
	private HashSet<UUID> rampUpDone;
	private HashSet<UUID> peakDone;
	private HashSet<UUID> rampDownDone;
	private HashSet<UUID> coolDownDone;
	private HashSet<UUID> terminateDone;
	private HashSet<UUID> warmupDone;

	private double _sigma;

	private double _kappa;
	
	public void request() {
		_timestamps.add(Clock.instance().millis());
		
		boolean locked = false;
		if (_locking != 0) {
			if (Math.random() < _locking) {
				_lock.writeLock().lock();
				locked = true;
			}
		}
		if (_workload != 0) {
			try {
				Thread.sleep(_workload);
			} catch (InterruptedException e) { }
		}
		if (locked) {
			_lock.writeLock().unlock();
		}
	}
	
	public synchronized void onInit(UUID node) {
		initDone.add(node);
	}
	
	public synchronized void onStart(UUID node) {
		startDone.add(node);
	}
	
	public synchronized void onRampUp(UUID node) {
		rampUpDone.add(node);
	}
	
	public synchronized void onPeak(UUID node, Run run) {
		peakDone.add(node);
		_runs.add(new Pair<UUID, Run>(node, run));
	}
	
	public synchronized void onRampDown(UUID node) {
		rampDownDone.add(node);
	}
	
	public synchronized void onCoolDown(UUID node) {
		coolDownDone.add(node);
	}
	
	public synchronized void onStop(UUID node, Run run) {
		boolean runDone = 
				initDone.equals(warmupDone) &&
				initDone.equals(rampUpDone) &&
				initDone.equals(peakDone) &&
				initDone.equals(rampDownDone) &&
				initDone.equals(coolDownDone);
		_runSuccessful.put(new Pair<UUID,Run>(node,run), runDone);
		if (!runDone) {
			System.out.println("WOOPS, seems run " + run + " by node " + node + " did not complete successfully!");
		}
	}
	
	public synchronized void onTerminate(UUID node) {
		terminateDone.add(node);
	}

	public synchronized void onWarmUp(UUID node) {
		warmupDone.add(node);
	}
	
	public int getTotalRequestCount() {
		return _timestamps.size();
	}
	
	public DescriptiveStatistics getInterArrivalStats() {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		
		for (int i = 0; i < _timestamps.size() - 1; i++) {
			stats.addValue(_timestamps.get(i + 1) - _timestamps.get(i));
		}
		
		return stats;
	}
	
	public int getNbRunsPerformed() {
		return _runs.size();
	}
	
	public boolean checkAllPhasesDone() {
		return initDone.equals(terminateDone) && checkAllRunsDone();
	}

	private boolean checkAllRunsDone() {
		if (_runs.size() != _runSuccessful.size()) return false;
		boolean done = true;
		for (Pair<UUID, Run> elem : _runSuccessful.keySet()) {
			done = done && _runSuccessful.get(elem);
		}
		return done;
	}

	public void setSigma(double sigma) {
		_sigma = sigma;
	}
	
	public void setKappa(double kappa) {
		_kappa = kappa;
	}
	
	public double getSigma() {
		return _sigma;
	}
	
	public double getKappa() {
		return _kappa;
	}
}
