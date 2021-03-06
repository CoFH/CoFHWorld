package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.distribution.base.AbstractStoneDistParser;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.world.distribution.Distribution;
import cofh.cofhworld.world.distribution.DistributionCustom;
import com.typesafe.config.Config;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

public class DistParserCustom extends AbstractStoneDistParser {

	private final String[] FIELDS = new String[] { "generator", "cluster-count", "x-offset", "y-offset", "z-offset" };

	@Override
	public String[] getRequiredFields() {

		return FIELDS;
	}

	@Override
	@Nonnull
	protected Distribution getFeature(String featureName, Config genObject, WorldGenerator gen, INumberProvider numClusters, boolean retrogen, Logger log) {

		INumberProvider xOffset = NumberData.parseNumberValue(genObject.getValue("x-offset"));
		INumberProvider yOffset = NumberData.parseNumberValue(genObject.getValue("y-offset"), 0, 255);
		INumberProvider zOffset = NumberData.parseNumberValue(genObject.getValue("z-offset"));

		return new DistributionCustom(featureName, gen, numClusters, retrogen, xOffset, yOffset, zOffset);
	}

}
