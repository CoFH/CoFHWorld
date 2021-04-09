package cofh.cofhworld.parser;

import com.typesafe.config.ConfigOrigin;

public class InvalidConfigurationException extends Exception {

	private final ConfigOrigin origin;

	public InvalidConfigurationException(String cause, ConfigOrigin origin) {

		super(cause);
		this.origin = origin;
	}

	public ConfigOrigin origin() {

		return this.origin;
	}

	public InvalidConfigurationException causedBy(Throwable cause) {

		this.initCause(cause);
		return this;
	}

}
