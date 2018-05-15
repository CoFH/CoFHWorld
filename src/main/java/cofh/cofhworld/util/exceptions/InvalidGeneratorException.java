package cofh.cofhworld.util.exceptions;

import com.typesafe.config.ConfigOrigin;

public class InvalidGeneratorException extends Exception {

	private final ConfigOrigin origin;

	public InvalidGeneratorException(String cause, ConfigOrigin origin) {

		super(cause);
		this.origin = origin;
	}

	public ConfigOrigin origin() {

		return this.origin;
	}

	public InvalidGeneratorException causedBy(Throwable cause) {

		this.initCause(cause);
		return this;
	}

}
