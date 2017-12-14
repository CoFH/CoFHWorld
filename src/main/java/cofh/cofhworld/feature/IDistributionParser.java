package cofh.cofhworld.feature;

import com.google.gson.JsonObject;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

public interface IDistributionParser {

	/**
	 * Parse a {@link JsonObject} for registration}.
	 *
	 * @param name      The name of the distribution to register.
	 * @param genObject The JsonObject to parse.
	 * @param log       The {@link Logger} to log debug/error/etc. messages to.
	 * @return The {@link IDistribution} to be registered
	 */
	IDistribution parse(String name, Config genObject, Logger log);

}
