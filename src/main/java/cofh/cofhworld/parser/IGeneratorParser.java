package cofh.cofhworld.parser;

import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.ConfigOrigin;

public interface IGeneratorParser<T extends IBuilder<? extends WorldGen>> {

	void getFields(IGeneratorFieldRegistry<T> fields);

	class InvalidGeneratorException extends Exception {

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
}
