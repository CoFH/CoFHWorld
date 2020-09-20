package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.distribution.base.AbstractDistParser;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.parser.variables.StringData;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.util.random.WeightedString;
import cofh.cofhworld.world.distribution.Distribution;
import cofh.cofhworld.world.distribution.DistributionUnderfluid;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.Config;
import net.minecraft.block.Blocks;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;

public class DistParserUnderfluid extends AbstractDistParser {

	private boolean isUnderwater;

	public DistParserUnderfluid(boolean water) {

		isUnderwater = water;
	}

	@Override
	protected List<WeightedBlock> generateDefaultMaterial() {

		return Arrays.asList(new WeightedBlock(Blocks.DIRT), new WeightedBlock(Blocks.GRASS));
	}

	@Override
	@Nonnull
	protected Distribution getFeature(String featureName, Config genObject, WorldGen gen, INumberProvider numClusters, boolean retrogen, Logger log) {

		boolean water = true;
		Set<String> fluidList = new HashSet<>();
		l:
		if (genObject.hasPath("fluid")) {
			ArrayList<WeightedString> list = new ArrayList<>();
			if (!StringData.parseStringList(genObject.getValue("fluid"), list)) {
				break l;
			}
			water = false;
			for (WeightedString str : list) {
				// ints.add(FluidRegistry.getFluidID(str.type));
				// NOPE. this NPEs.
				fluidList.add(str.value);
			}
		}

		// does this logic actually need a material?
		List<WeightedBlock> matList = defaultMaterial;
		if (genObject.hasPath("material")) {
			matList = new ArrayList<>();
			if (!BlockData.parseBlockList(genObject.getValue("material"), matList, false)) {
				log.warn("Invalid material list! Using default list.");
				matList = defaultMaterial;
			}
		}
		if (water) {
			return new DistributionUnderfluid(featureName, gen, matList, numClusters, retrogen);
		} else {
			return new DistributionUnderfluid(featureName, gen, matList, fluidList.toArray(new String[0]), numClusters, retrogen);
		}
	}

	@Override
	protected String getDefaultGenerator() {

		return isUnderwater ? "plate" : super.getDefaultGenerator();
	}

}
