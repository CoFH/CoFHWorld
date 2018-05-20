package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.distribution.base.AbstractStoneDistParser;
import cofh.cofhworld.parser.variables.NumberData;
import cofh.cofhworld.world.distribution.Distribution;
import cofh.cofhworld.world.distribution.DistributionCave;
import com.typesafe.config.Config;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.Logger;

public class DistParserCave extends AbstractStoneDistParser {

	@Override
	protected Distribution getFeature(String featureName, Config genObject, WorldGenerator gen, INumberProvider numClusters, boolean retrogen, Logger log) {

		boolean ceiling = genObject.hasPath("ceiling") && genObject.getBoolean("ceiling");
		DistributionCave cave = new DistributionCave(featureName, gen, ceiling, numClusters, retrogen);
		if (genObject.hasPath("ground-level")) {
			cave.setGroundLevel(NumberData.parseNumberValue(genObject.getValue("ground-level"), 0, 255));
		}
		return cave;
	}

}
