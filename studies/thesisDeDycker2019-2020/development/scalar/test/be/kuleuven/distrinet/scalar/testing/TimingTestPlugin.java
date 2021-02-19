package be.kuleuven.distrinet.scalar.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import be.kuleuven.distrinet.scalar.config.Option;
import be.kuleuven.distrinet.scalar.core.Plugin;
import be.kuleuven.distrinet.scalar.exceptions.DataException;
import be.kuleuven.distrinet.scalar.exceptions.ExperimentalException;
import be.kuleuven.distrinet.scalar.exceptions.InternalException;
import be.kuleuven.distrinet.scalar.experiment.Experiment;
import be.kuleuven.distrinet.scalar.experiment.Run;
import be.kuleuven.distrinet.scalar.plugin.GnuPlotGenerator;
import be.kuleuven.distrinet.scalar.reporting.Report;
import be.kuleuven.distrinet.scalar.requests.Request;

public class TimingTestPlugin extends Plugin {

	private TimingTester _tester;

	private UUID _id;

	public TimingTestPlugin() {
		_tester = TimingTester.instance();
	}

	@Override
	protected void onRequest(Request r) {
	}

	@Override
	protected void onStartUp() {
		_tester.onStart(_id);
	}

	@Override
	protected void onWarmUp() {
		_tester.onWarmUp(_id);
	}

	@Override
	protected void onRampUp() {
		_tester.onRampUp(_id);
	}

	@Override
	protected void onPeak() {
		try {
			Run run = ((Experiment) data().get(Option.RUN_EXPERIMENT.toString())).lastRun();
			_tester.onPeak(_id, run);
		} catch (DataException | ExperimentalException e) {
			throw new InternalException("Error during establishing peak load/duration.");
		}
	}

	@Override
	protected void onRampDown() {
		_tester.onRampDown(_id);
	}

	@Override
	protected void onCoolDown() {
		_tester.onCoolDown(_id);
	}

	@Override
	protected void onStop(ArrayList<Request> allRequests) {
		try {
			Run run = ((Experiment) data().get(Option.RUN_EXPERIMENT.toString())).lastRun();
			_tester.onStop(_id, run);
		} catch (DataException | ExperimentalException e) {
			throw new InternalException("Error during establishing stop load/duration.");
		}
	}

	@Override
	protected void onTermination() {
		_tester.onTerminate(_id);
		try {
			_tester.setSigma(data().getAsDouble("gnuplot:sigma"));
			_tester.setKappa(data().getAsDouble("gnuplot:kappa"));
		} catch (DataException e) { }
	}

	@Override
	protected void onInitialization() {
		try {
			_id = (UUID) data().get(Option.LOCAL_ID.toString());
		} catch (Throwable e) {
			e.printStackTrace();
			throw new InternalException("I have no ID.");
		}
		_tester.onInit(_id);
	}

}
