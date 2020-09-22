package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.distribution.base.AbstractDistParser;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.world.distribution.Distribution;
import cofh.cofhworld.world.distribution.DistributionLargeVein;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

public class DistParserLargeVein extends AbstractDistParser {

	private final String[] FIELDS = new String[] { "generator", "cluster-count", "min-height", "vein-height", "vein-diameter", "vertical-density", "horizontal-density" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	@Nonnull
	protected Distribution getFeature(String featureName, Config genData, WorldGen gen, INumberProvider numClusters, boolean retrogen, Logger log) {

		INumberProvider minY = NumberData.parseNumberValue(genData.getValue("min-height"));
		INumberProvider h = NumberData.parseNumberValue(genData.getValue("vein-height"));
		INumberProvider d = NumberData.parseNumberValue(genData.getValue("vein-diameter"));
		INumberProvider vD = NumberData.parseNumberValue(genData.getValue("vertical-density"), 0, 100);
		INumberProvider hD = NumberData.parseNumberValue(genData.getValue("horizontal-density"), 0, 100);

		return new DistributionLargeVein(featureName, gen, numClusters, minY, retrogen, h, d, vD, hD);
	}

	@Override
	protected String getDefaultGenerator() {

		return "large-vein";
	}

}
