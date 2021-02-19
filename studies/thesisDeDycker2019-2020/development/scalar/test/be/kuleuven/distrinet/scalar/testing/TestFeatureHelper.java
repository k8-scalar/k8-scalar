package be.kuleuven.distrinet.scalar.testing;

import be.kuleuven.distrinet.scalar.exceptions.DataException;
import be.kuleuven.distrinet.scalar.exceptions.InternalException;
import be.kuleuven.distrinet.scalar.util.FeatureHelper;

public class TestFeatureHelper extends FeatureHelper {

	private boolean _start1;
	private boolean _start2;
	private boolean _start3;
	private boolean _stop1;
	private boolean _stop2;
	private boolean _init;

	public TestFeatureHelper() {
		_init = false;
		_start1 = false;
		_start2 = false;
		_start3 = false;
		_stop1 = false;
		_stop2 = false;
	}
	
	public void doStart1() {
		_start1 = true;
	}

	public void doStart2() {
		_start2 = _start1; // True if _start1 already happened.
	}

	public void doStart3() {
		_start3 = true;
	}

	public void doStop1() {
		_stop1 = _start2; // True if _start2 already happened.
	}

	public void doStop2() {
		_stop2 = _stop1; // True if _stop1 already happened.
	}

	public boolean doCheck() {
		return _init && _start1 && _start2 && _start3 && _stop1 && _stop2;
	}

	@Override
	public void onInitialization() {
		_init = true;
	}

	@Override
	public void onTermination() {
		if (doCheck())
			try {
				data().put("testing:TestFeatureHelper", "succeeded");
			} catch (DataException e) {
				throw new InternalException(e);
			}
	}
}
