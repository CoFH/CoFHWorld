package cofh.cofhworld.parser;

import cofh.cofhworld.parser.IDistributionParser.InvalidDistributionException;
import cofh.cofhworld.world.IFeatureGenerator;
import com.typesafe.config.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DistributionData {

	public static IFeatureGenerator parseFeature(String featureName, Config distObject) throws InvalidDistributionException {

		return IDistributionParser.parseFeature(featureName, distObject);
	}

	public static List<IFeatureGenerator> parseFeatures(ConfigValue distObject) throws InvalidDistributionException {

		switch (distObject.valueType()) {
			case OBJECT:
				return Collections.singletonList(parseFeature("<INVALID>", ((ConfigObject)distObject).toConfig()));
			case LIST:
				ArrayList<IFeatureGenerator> features = new ArrayList<>();
				for (ConfigValue val : ((ConfigList)distObject)) {
					if (val.valueType() != ConfigValueType.NULL) features.add(getFeature(val));
				}
				return features;
			default:
				throw new InvalidDistributionException("Feature must be an Object or Array to be parsed.", distObject.origin());
		}
	}

	public static IFeatureGenerator getFeature(ConfigValue distObject) throws InvalidDistributionException {

		if (distObject.valueType() == ConfigValueType.OBJECT) {
			return IDistributionParser.parseFeature("<INVALID>", ((ConfigObject)distObject).toConfig());
		}
		throw new InvalidDistributionException("Feature must be an Object to be parsed.", distObject.origin());
	}
}
