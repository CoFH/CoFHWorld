package cofh.cofhworld.parser;

import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.parser.IDistributionParser.InvalidDistributionException;
import cofh.cofhworld.world.IConfigurableFeatureGenerator;
import cofh.cofhworld.world.IFeatureGenerator;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import static cofh.cofhworld.CoFHWorld.log;

public class DistributionData {

	public static IFeatureGenerator parseFeature(String featureName, Config distObject) throws InvalidDistributionException {

		String featureType = parseFeatureType(distObject);
		IDistributionParser parser = FeatureParser.getDistribution(featureType);
		if (parser != null) {
			boolean missedFields = false;
			for (String field : parser.getRequiredFields()) {
				if (!distObject.hasPath(field)) {
					log.error("Missing required setting `{}` for distribution type '{}' on feature '{}' at line {}.", field, featureType, featureName, distObject.origin().lineNumber());
					missedFields = true;
				}
			}
			if (missedFields) {
				throw new InvalidDistributionException("Missing required fields", distObject.origin());
			}
			return parser.parseFeature(featureName, distObject, log);
		} else {
			log.warn("Unknown distribution '{}' on line {}.", featureType, distObject.origin().lineNumber());
			throw new InvalidDistributionException("Unknown distribution type", distObject.origin());
		}
	}

	public static IConfigurableFeatureGenerator getFeature(String featureName, Config distObject, boolean retrogen, Logger log) throws InvalidDistributionException {

		String featureType = parseFeatureType(distObject);
		IDistributionParser parser = FeatureParser.getDistribution(featureType);
		if (parser != null) {
			boolean missedFields = false;
			for (String field : parser.getRequiredFields()) {
				if (!distObject.hasPath(field)) {
					log.error("Missing required setting `{}` for distribution type '{}' on feature '{}' at line {}.", field, featureType, featureName, distObject.origin().lineNumber());
					missedFields = true;
				}
			}
			if (missedFields) {
				throw new InvalidDistributionException("Missing required fields", distObject.origin());
			}
			return parser.getFeature(featureName, distObject, retrogen, log);
		} else {
			log.warn("Unknown distribution '{}' on line {}.", featureType, distObject.origin().lineNumber());
			throw new InvalidDistributionException("Unknown distribution type", distObject.origin());
		}
	}

	public static String parseFeatureType(Config genObject) {

		return genObject.getString("distribution");
	}
}
