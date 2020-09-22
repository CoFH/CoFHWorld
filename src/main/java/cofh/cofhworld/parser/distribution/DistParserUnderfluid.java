package cofh.cofhworld.parser.distribution;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.distribution.base.AbstractDistParser;
import cofh.cofhworld.parser.variables.BlockData;
import cofh.cofhworld.parser.variables.StringData;
import cofh.cofhworld.util.random.WeightedString;
import cofh.cofhworld.world.distribution.Distribution;
import cofh.cofhworld.world.distribution.DistributionUnderfluid;
import cofh.cofhworld.world.generator.WorldGen;
import com.typesafe.config.Config;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DistParserUnderfluid extends AbstractDistParser {

	private boolean isUnderwater;

	public DistParserUnderfluid(boolean water) {

		isUnderwater = water;
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
		List<Material> matList = new ArrayList<>();
		if (genObject.hasPath("material")) {
			if (!BlockData.parseMaterialList(genObject.getValue("material"), matList)) {
				log.warn("Invalid material list! A partial list will be used.");
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
