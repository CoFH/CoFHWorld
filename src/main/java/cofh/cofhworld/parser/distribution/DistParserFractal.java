package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.parser.distribution.base.AbstractStoneDistParser;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.world.distribution.Distribution;
import cofh.cofhworld.world.IConfigurableFeatureGenerator.GenRestriction;
import cofh.cofhworld.world.distribution.DistributionLargeVein;
import cofh.cofhworld.util.numbers.INumberProvider;
import com.typesafe.config.Config;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

public class DistParserFractal extends AbstractStoneDistParser {

	private final String[] FIELDS = new String[] { "generator", "min-height", "vein-height", "vein-diameter", "vertical-density", "horizontal-density" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	protected Distribution getFeature(String featureName, Config genData, WorldGenerator gen, INumberProvider numClusters, GenRestriction biomeRes, boolean retrogen, GenRestriction dimRes, Logger log) {

		INumberProvider minY = NumberData.parseNumberValue(genData.getValue("min-height"));
		INumberProvider h = NumberData.parseNumberValue(genData.getValue("vein-height"));
		INumberProvider d = NumberData.parseNumberValue(genData.getValue("vein-diameter"));
		INumberProvider vD = NumberData.parseNumberValue(genData.getValue("vertical-density"), 0, 100);
		INumberProvider hD = NumberData.parseNumberValue(genData.getValue("horizontal-density"), 0, 100);

		return new DistributionLargeVein(featureName, gen, numClusters, minY, biomeRes, retrogen, dimRes, h, d, vD, hD);
	}

	@Override
	protected String getDefaultGenerator() {

		return "large-vein";
	}

}
