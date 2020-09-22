package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.distribution.base.AbstractDistParser;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.world.distribution.Distribution;
import cofh.cofhworld.world.distribution.DistributionUniform;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

public class DistParserUniform extends AbstractDistParser {

	private final String[] FIELDS = new String[] { "generator", "cluster-count", "min-height", "max-height" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	@Nonnull
	protected Distribution getFeature(String featureName, Config genObject, WorldGen gen, INumberProvider numClusters, boolean retrogen, Logger log) {

		INumberProvider minHeight = NumberData.parseNumberValue(genObject.getValue("min-height"));
		INumberProvider maxHeight = NumberData.parseNumberValue(genObject.getValue("max-height"));

		return new DistributionUniform(featureName, gen, numClusters, minHeight, maxHeight, retrogen);
	}

}
