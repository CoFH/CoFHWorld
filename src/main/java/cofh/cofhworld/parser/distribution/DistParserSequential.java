package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.parser.DistributionData;
import cofh.cofhworld.parser.IDistributionParser;
import cofh.cofhworld.world.IConfigurableFeatureGenerator;
import cofh.cofhworld.world.distribution.Distribution;
import cofh.cofhworld.world.distribution.DistributionSequential;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class DistParserSequential implements IDistributionParser {

	private final String[] FIELDS = new String[] { "features" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	@Nonnull
	public Distribution getFeature(String featureName, Config genObject, boolean retrogen, Logger log) throws InvalidDistributionException {

		ArrayList<IConfigurableFeatureGenerator> features = new ArrayList<>();

		int i = 0;
		for (Config featureConf : genObject.getConfigList("features")) {
			features.add(DistributionData.getFeature(featureName + '$' + ++i, featureConf, retrogen, log));
		}

		return new DistributionSequential(featureName, features, retrogen);
	}

}
