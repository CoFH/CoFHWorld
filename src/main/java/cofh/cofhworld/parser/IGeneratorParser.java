package cofh.cofhworld.parser;

import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.IFeatureGenerator;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigOrigin;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.List;

public interface IGeneratorParser {

	/**
	 * Parse a {@link Config} for usage with an {@link IFeatureGenerator}.
	 *
	 * @param name      The name of the generator entry.
	 * @param genObject The JsonObject to parse.
	 * @param log       The {@link Logger} to log debug/error/etc. messages to.
	 * @param resList   The processed list of resources to generate
	 * @param matList   The processed list of materials to generate in
	 * @return The {@link WorldGen} to be registered with an IFeatureGenerator
	 */
	@Nonnull
	WorldGen parseGenerator(String name, Config genObject, Logger log, List<WeightedBlock> resList, List<WeightedBlock> matList) throws InvalidGeneratorException;

	String[] getRequiredFields();

	default boolean isMeta() {

		return false;
	}

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
