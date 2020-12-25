package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.distribution.base.AbstractDistParser;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.world.distribution.Distribution;
import cofh.cofhworld.world.distribution.DistributionSurface;
import cofh.cofhworld.world.distribution.DistributionTopBlock;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class DistParserSurface extends AbstractDistParser {

	@Override
	@Nonnull
	protected Distribution getFeature(String featureName, Config genObject, WorldGen gen, INumberProvider numClusters, boolean retrogen, Logger log) {

		// this feature checks the block below where the generator runs, and needs its own material list
		List<Material> matList = new ArrayList<>();
		if (genObject.hasPath("material")) {
			if (!BlockData.parseMaterialList(genObject.getValue("material"), matList)) {
				log.warn("Invalid material list! A partial list will be used.");
			}
		}

		if (genObject.hasPath("top-block") && genObject.getBoolean("top-block")) {
			return new DistributionTopBlock(featureName, gen, matList, numClusters, retrogen);
		} else {
			return new DistributionSurface(featureName, gen, matList, numClusters, retrogen);
		}
	}

}
