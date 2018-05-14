package cofh.cofhworld.feature.parser;

import cofh.cofhworld.feature.generator.FeatureBase;
import cofh.cofhworld.feature.IConfigurableFeatureGenerator.GenRestriction;
import cofh.cofhworld.feature.generator.FeatureGenUnderfluid;
import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.util.WeightedRandomString;
import cofh.cofhworld.util.numbers.INumberProvider;
import com.typesafe.config.Config;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.DungeonHooks.DungeonMob;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class UnderfluidParser extends UniformParser {

	private boolean isUnderwater;

	public UnderfluidParser(boolean water) {

		isUnderwater = water;
	}

	@Override
	protected List<WeightedRandomBlock> generateDefaultMaterial() {

		return Arrays.asList(new WeightedRandomBlock(Blocks.DIRT, -1), new WeightedRandomBlock(Blocks.GRASS, -1));
	}

	@Override
	protected FeatureBase getFeature(String featureName, Config genObject, WorldGenerator gen, INumberProvider numClusters, GenRestriction biomeRes, boolean retrogen, GenRestriction dimRes, Logger log) {

		boolean water = true;
		Set<String> fluidList = new HashSet<>();
		l:
		if (genObject.hasPath("fluid")) {
			ArrayList<WeightedRandomString> list = new ArrayList<>();
			if (!FeatureParser.parseWeightedStringList(genObject.root().get("fluid"), list)) {
				break l;
			}
			water = false;
			for (WeightedRandomString str : list) {
				// ints.add(FluidRegistry.getFluidID(str.type));
				// NOPE. this NPEs.
				Fluid fluid = FluidRegistry.getFluid(str.value);
				if (fluid != null) {
					fluidList.add(fluid.getName());
				}
			}
		}

		// TODO: WorldGeneratorAdv that allows access to its material list
		List<WeightedRandomBlock> matList = defaultMaterial;
		if (genObject.hasPath("material")) {
			matList = new ArrayList<>();
			if (!FeatureParser.parseResList(genObject.root().get("material"), matList, false)) {
				log.warn("Invalid material list! Using default list.");
				matList = defaultMaterial;
			}
		}
		if (water) {
			return new FeatureGenUnderfluid(featureName, gen, matList, numClusters, biomeRes, retrogen, dimRes);
		} else {
			return new FeatureGenUnderfluid(featureName, gen, matList, fluidList.toArray(new String[0]), numClusters, biomeRes, retrogen, dimRes);
		}
	}

	@Override
	protected String getDefaultGenerator() {

		return isUnderwater ? "plate" : super.getDefaultGenerator();
	}

}
