package cofh.cofhworld.parser;

import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.world.IFeatureGenerator;
import com.typesafe.config.Config;

import static cofh.cofhworld.CoFHWorld.log;

public class DistributionData {

	public static IFeatureGenerator parseFeature(String featureName, Config distObject) {

		String templateName = parseFeatureType(distObject);
		IDistributionParser template = FeatureParser.getDistribution(templateName);
		if (template != null) {
			IFeatureGenerator feature = template.parseFeature(featureName, distObject, log);
			if (feature != null) {
				return feature;
			}
			log.error("Distribution '{}' failed to parse its entry on line {}!", templateName, distObject.origin().lineNumber());
		} else {
			log.warn("Unknown distribution '{}' on line {}.", templateName, distObject.origin().lineNumber());
		}
		// TODO: throw exception instead
		return null;
	}

	public static String parseFeatureType(Config genObject) {

		return genObject.getString("distribution");
	}
}
