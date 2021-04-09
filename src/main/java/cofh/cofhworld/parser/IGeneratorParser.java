package cofh.cofhworld.parser;

import cofh.cofhworld.parser.IBuilder.IBuilderFieldRegistry;
import com.typesafe.config.ConfigOrigin;

public interface IGeneratorParser<T, B extends IBuilder<T>> {

	void getFields(IBuilderFieldRegistry<T, B> fields);

	class InvalidGeneratorException extends InvalidConfigurationException {

		public InvalidGeneratorException(String cause, ConfigOrigin origin) {

			super(cause, origin);
		}

		public InvalidGeneratorException causedBy(Throwable cause) {

			this.initCause(cause);
			return this;
		}

	}
}
