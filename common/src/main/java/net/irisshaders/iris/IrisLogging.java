package net.irisshaders.iris;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IrisLogging {
	public static final boolean ENABLE_SPAM = false; // IrisPlatformHelpers.getInstance().isDevelopmentEnvironment();

	private final Logger logger;

	public IrisLogging(String loggerName) {
		this.logger = LogManager.getLogger(loggerName);
	}

	public void fatal(String fatal) {
		this.logger.fatal(fatal);
	}

	public void fatal(String fatal, Throwable t) {
		this.logger.fatal(fatal, t);
	}

	public void error(String error) {
		this.logger.error(error);
	}

	public void error(String error, Object... o) {
		this.logger.error(error, o);
	}

	public void error(String error, Throwable t) {
		this.logger.error(error, t);
	}

	public void warn(String warning) {
		this.logger.warn(warning);
	}

	public void warn(String warning, Object... object) {
		this.logger.warn(warning, object);
	}

	public void warn(String warning, Throwable t) {
		this.logger.warn(warning, t);
	}

	public void warn(Throwable o) {
		this.logger.warn("", o);
	}

	public void info(String info) {
		this.logger.info(info);
	}

	public void info(String info, Object... o) {
		this.logger.info(info, o);
	}

	public void debug(String debug) {
		this.logger.debug(debug);
	}

	public void debug(String debug, Throwable t) {
		this.logger.debug(debug, t);
	}
}
