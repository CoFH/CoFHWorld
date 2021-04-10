package cofh.cofhworld.parser;

import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.parser.IDistributionParser.InvalidDistributionException;
import cofh.cofhworld.world.IConfigurableFeatureGenerator;
import cofh.cofhworld.world.IFeatureGenerator;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import org.apache.logging.log4j.Logger;

import static cofh.cofhworld.CoFHWorld.log;

public class DistributionData {

	public static IFeatureGenerator parseFeature(String featureName, Config distObject) throws InvalidDistributionException {

		String featureType = parseFeatureType(distObject);
		IDistributionParser parser = FeatureParser.getDistribution(featureType);
		if (parser == null) {
			log.warn("Unknown distribution '{}' on line {}.", featureType, distObject.origin().lineNumber());
			throw new InvalidDistributionException("Unknown distribution type", distObject.origin());
		}

		boolean missedFields = false;
		for (String field : parser.getRequiredFields()) {
			log.trace("Checking for field {}", field);
			if (!distObject.hasPath(field)) {
				log.error("Missing required setting `{}` for distribution type '{}' on feature '{}' at line {}.", field, featureType, featureName,
						distObject.origin().lineNumber());
				missedFields = true;
			}
		}
		if (missedFields) {
			throw new InvalidDistributionException("Missing required fields", distObject.origin());
		}
		return parser.parseFeature(featureName, distObject, log);
	}

	public static IConfigurableFeatureGenerator getFeature(String featureName, Config distObject, boolean retrogen, Logger log) throws InvalidDistributionException {

		String featureType = parseFeatureType(distObject);
		IDistributionParser parser = FeatureParser.getDistribution(featureType);
		if (parser == null) {
			log.warn("Unknown distribution '{}' on line {}.", featureType, distObject.origin().lineNumber());
			throw new InvalidDistributionException("Unknown distribution type", distObject.origin());
		}

		boolean missedFields = false;
		for (String field : parser.getRequiredFields()) {
			log.trace("Checking for field {}", field);
			if (!distObject.hasPath(field)) {
				log.error("Missing required setting `{}` for distribution type '{}' on feature '{}' at line {}.", field, featureType, featureName,
						distObject.origin().lineNumber());
				missedFields = true;
			}
		}
		if (missedFields) {
			throw new InvalidDistributionException("Missing required fields", distObject.origin());
		}
		return parser.getFeature(featureName, distObject, retrogen, log);
	}

	public static String parseFeatureType(Config genObject) throws InvalidDistributionException {

		final String FIELD = "distribution";

		String name;
		if (genObject.hasPath(FIELD)) {
			ConfigValue typeVal = genObject.getValue(FIELD);
			if (typeVal.valueType() == ConfigValueType.STRING) {
				name = String.valueOf(typeVal.unwrapped());
			} else if (typeVal.valueType() == ConfigValueType.OBJECT && genObject.hasPath(FIELD + ".name")) {
				name = genObject.getString(FIELD + ".name");
			} else {
				throw new InvalidDistributionException("Feature `distribution` entry not valid", genObject.origin());
			}
		} else {
			throw new InvalidDistributionException("Feature `distribution` entry is not specified!", genObject.origin());
		}
		return name;
	}
}
