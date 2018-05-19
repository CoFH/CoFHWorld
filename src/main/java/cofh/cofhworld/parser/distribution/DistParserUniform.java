package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.parser.distribution.base.AbstractStoneDistParser;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.util.numbers.INumberProvider;
import cofh.cofhworld.world.IConfigurableFeatureGenerator.GenRestriction;
import cofh.cofhworld.world.distribution.Distribution;
import cofh.cofhworld.world.distribution.DistributionUniform;
import com.typesafe.config.Config;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

public class DistParserUniform extends AbstractStoneDistParser {

	private final String[] FIELDS = new String[] { "generator", "min-height", "max-height" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	protected Distribution getFeature(String featureName, Config genObject, WorldGenerator gen, INumberProvider numClusters, GenRestriction biomeRes, boolean retrogen, GenRestriction dimRes, Logger log) {

		if (!(genObject.hasPath("min-height") && genObject.hasPath("max-height"))) {
			log.error("Height parameters for 'uniform' template not specified in \"" + featureName + "\"");
			return null;
		}

		INumberProvider minHeight = NumberData.parseNumberValue(genObject.root().get("min-height"));
		INumberProvider maxHeight = NumberData.parseNumberValue(genObject.root().get("max-height"));

		return new DistributionUniform(featureName, gen, numClusters, minHeight, maxHeight, biomeRes, retrogen, dimRes);
	}

}
