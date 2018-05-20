package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.distribution.base.AbstractStoneDistParser;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.world.distribution.Distribution;
import cofh.cofhworld.world.distribution.DistributionUniform;
import com.typesafe.config.Config;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

public class DistParserUniform extends AbstractStoneDistParser {

	private final String[] FIELDS = new String[] { "generator", "cluster-count", "min-height", "max-height" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	protected Distribution getFeature(String featureName, Config genObject, WorldGenerator gen, INumberProvider numClusters, boolean retrogen, Logger log) {

		INumberProvider minHeight = NumberData.parseNumberValue(genObject.getValue("min-height"));
		INumberProvider maxHeight = NumberData.parseNumberValue(genObject.getValue("max-height"));

		return new DistributionUniform(featureName, gen, numClusters, minHeight, maxHeight, retrogen);
	}

}
