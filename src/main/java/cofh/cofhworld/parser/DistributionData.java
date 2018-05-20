package cofh.cofhworld.parser;

import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.world.IConfigurableFeatureGenerator;
import cofh.cofhworld.world.IFeatureGenerator;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import static cofh.cofhworld.CoFHWorld.log;

public class DistributionData {

	public static IFeatureGenerator parseFeature(String featureName, Config distObject) {

		String featureType = parseFeatureType(distObject);
		IDistributionParser parser = FeatureParser.getDistribution(featureType);
		if (parser != null) {
			boolean missedFields = false;
			for (String field : parser.getRequiredFields()) {
				if (!distObject.hasPath(field)) {
					log.error("Missing required setting `{}` for distribution type '{}' on feature '{}' at line {}.",
							field, featureType, featureName, distObject.origin().lineNumber());
					missedFields = true;
				}
			}
			if (missedFields) {
				return null;
			}
			IFeatureGenerator feature = parser.parseFeature(featureName, distObject, log);
			if (feature != null) {
				return feature;
			}
			log.error("Distribution '{}' failed to parse its entry on line {}!", featureType, distObject.origin().lineNumber());
		} else {
			log.warn("Unknown distribution '{}' on line {}.", featureType, distObject.origin().lineNumber());
		}
		// TODO: throw exception instead
		return null;
	}

	public static IConfigurableFeatureGenerator getFeature(String featureName, Config distObject, IConfigurableFeatureGenerator.GenRestriction biomeRes,
			boolean retrogen, IConfigurableFeatureGenerator.GenRestriction dimRes, Logger log) {

		String featureType = parseFeatureType(distObject);
		IDistributionParser parser = FeatureParser.getDistribution(featureType);
		if (parser != null) {
			boolean missedFields = false;
			for (String field : parser.getRequiredFields()) {
				if (!distObject.hasPath(field)) {
					log.error("Missing required setting `{}` for distribution type '{}' on feature '{}' at line {}.",
							field, featureType, featureName, distObject.origin().lineNumber());
					missedFields = true;
				}
			}
			if (missedFields) {
				return null;
			}
			IConfigurableFeatureGenerator feature = parser.getFeature(featureName, distObject, biomeRes, retrogen, dimRes, log);
			if (feature != null) {
				return feature;
			}
			log.error("Distribution '{}' failed to parse its entry on line {}!", featureType, distObject.origin().lineNumber());
		} else {
			log.warn("Unknown distribution '{}' on line {}.", featureType, distObject.origin().lineNumber());
		}
		// TODO: throw exception instead
		return null;
	}

	public static String parseFeatureType(Config genObject) {

		return genObject.getString("distribution");
	}
}
