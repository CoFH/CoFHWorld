package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.parser.DistributionData;
import cofh.cofhworld.parser.IDistributionParser;
import cofh.cofhworld.world.IConfigurableFeatureGenerator;
import cofh.cofhworld.world.IConfigurableFeatureGenerator.GenRestriction;
import cofh.cofhworld.world.distribution.Distribution;
import cofh.cofhworld.world.distribution.DistributionSequential;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class DistParserSequential implements IDistributionParser {

	private final String[] FIELDS = new String[] { "features" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	public Distribution getFeature(String featureName, Config genObject, GenRestriction biomeRes, boolean retrogen, GenRestriction dimRes, Logger log) {

		ArrayList<IConfigurableFeatureGenerator> features = new ArrayList<>();

		int i = 0;
		for (Config featureConf : genObject.getConfigList("features")) {
			IConfigurableFeatureGenerator feature = DistributionData.getFeature(featureName + '$' + ++i, featureConf, biomeRes, retrogen, dimRes, log);
			if (feature == null) {
				return null;
			}
			features.add(feature);
		}

		return new DistributionSequential(featureName, features, biomeRes, retrogen, dimRes);
	}

}
