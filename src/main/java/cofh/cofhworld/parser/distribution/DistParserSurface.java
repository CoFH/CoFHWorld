package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.distribution.base.AbstractDistParser;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.distribution.Distribution;
import cofh.cofhworld.world.distribution.DistributionSurface;
import cofh.cofhworld.world.distribution.DistributionTopBlock;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.Config;
import net.minecraft.block.Blocks;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DistParserSurface extends AbstractDistParser {

	@Override
	protected List<WeightedBlock> generateDefaultMaterial() {

		return Arrays.asList(new WeightedBlock(Blocks.STONE), new WeightedBlock(Blocks.DIRT), new WeightedBlock(Blocks.GRASS), new WeightedBlock(Blocks.SAND), new WeightedBlock(Blocks.GRAVEL), new WeightedBlock(Blocks.SNOW), new WeightedBlock(Blocks.AIR), new WeightedBlock(Blocks.WATER));
	}

	@Override
	@Nonnull
	protected Distribution getFeature(String featureName, Config genObject, WorldGen gen, INumberProvider numClusters, boolean retrogen, Logger log) {

		// this feature checks the block below where the generator runs, and needs its own material list
		List<WeightedBlock> matList = defaultMaterial;
		if (genObject.hasPath("material")) {
			matList = new ArrayList<>();
			if (!BlockData.parseBlockList(genObject.getValue("material"), matList, false)) {
				log.warn("Invalid material list! Using default list.");
				matList = defaultMaterial;
			}
		}
		// TODO: rename follow-terrain?: when true, stops at first found block, when false finds first solid non-tree block
		if (genObject.hasPath("follow-terrain") && genObject.getBoolean("follow-terrain")) {
			return new DistributionTopBlock(featureName, gen, matList, numClusters, retrogen);
		} else {
			return new DistributionSurface(featureName, gen, matList, numClusters, retrogen);
		}
	}

}
