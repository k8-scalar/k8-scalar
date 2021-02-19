package be.kuleuven.distrinet.scalar.testing;

import java.util.Properties;

public class TestUtils {

	public static Properties getMinimalTestConfig() {
		Properties props = new Properties();
		props.put("think_time", "1000");
		props.put("user_peak_load", "1");
		props.put("user_implementations", "be.kuleuven.distrinet.scalar.users.TestUser:1");
		props.put("user_peak_duration", "1");
		props.put("user_warmup_duration", "0");
		props.put("user_wait_inbetween_runs", "0");
		props.put("internal_start_command_server", "false");
		props.put("local:experimentalPropertiesLocation", "testing");
		props.put("plugins",
			"be.kuleuven.distrinet.scalar.plugin.ExperimentalResultsPublisher");
		props.put("internal_data_providers", "LocalStorageProvider,TestingDataProvider");
		props.put("mode", "manual");
		props.put("internal_timeout_period", "20");
		return props;
	}
	
	public static Properties getExperimentTestConfig() {
		Properties props = getMinimalTestConfig();
		props.put("user_peak_load", "1,100,200,300,400");
		props.put("think_time", "1000");
		props.put("user_warmup_duration", "1");
		props.put("user_warmup_fraction", "1.0");
		props.put("user_peak_duration", "5");
		props.put("user_implementations", "be.kuleuven.distrinet.scalar.testing.TimingTestUser:1");
		props.put("think_time_strategy_factory", "ExponentialThinkTimeStrategyFactory");
		props.put("plugins", props.get("plugins") + ",be.kuleuven.distrinet.scalar.plugin.GnuPlotGenerator,be.kuleuven.distrinet.scalar.testing.TimingTestPlugin");
		props.put("gnuplot_binary", "/usr/local/bin/gnuplot");
		props.put("gnuplot_config", "conf/plot.gnu");
		props.put("internal_debug_enabled", "true");
		props.put("debug_filename", "junit_thread_dump.txt");
		return props;
	}
	
	public static Properties getAutoScalarTestConfig() {
		Properties props = getExperimentTestConfig();
		props.put("think_time", "1000");
		props.put("local:experimentalPropertiesLocation", "testing");
		props.put("plugins",
			"be.kuleuven.distrinet.scalar.plugin.ExperimentalResultsPublisher," +
			"be.kuleuven.distrinet.scalar.testing.TimingTestPlugin");
		props.put("mode", "auto");
		return props;
	}
	
}
