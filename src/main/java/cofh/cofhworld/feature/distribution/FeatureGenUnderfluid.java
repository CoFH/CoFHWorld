package cofh.cofhworld.feature.distribution;

import cofh.cofhworld.feature.Feature;
import cofh.cofhworld.feature.IDistribution;
import cofh.cofhworld.feature.IDistributionParser;
import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.util.Utils;
import cofh.cofhworld.util.WeightedRandomBlock;
import com.typesafe.config.Config;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DungeonHooks;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class FeatureGenUnderfluid implements IDistribution {

	final boolean water;
	final List<WeightedRandomBlock> matList;
	final String[] fluidList;

	public FeatureGenUnderfluid(List<WeightedRandomBlock> matList, String[] fluidList) {

		this.matList = matList;
		this.water = (fluidList.length == 0); // TODO: Also check for single water block in list?
		this.fluidList = fluidList;
	}

	@Override
	public boolean apply(Feature f, Random random, int blockX, int blockZ, World world) {

		BlockPos pos = new BlockPos(blockX, 64, blockZ);

		final int count = f.getChunkCount().intValue(world, random, pos);

		boolean generated = false;
		for (int i = 0; i < count; i++) {
			int x = blockX + random.nextInt(16);
			int z = blockZ + random.nextInt(16);
			if (!f.canGenerateInBiome(world, x, z, random)) {
				continue;
			}

			int y = Utils.getSurfaceBlockY(world, x, z);
			l:
			do {
				IBlockState state = world.getBlockState(new BlockPos(x, y, z));
				if (water) {
					if (state.getMaterial() == Material.WATER) {
						continue;
					}
					if (world.getBlockState(new BlockPos(x, y + 1, z)).getMaterial() != Material.WATER) {
						continue;
					}
				} else {
					// TODO: Revisit this for efficiency?? Seems like a hash-set should work
					Fluid fluid = Utils.lookupFluidForBlock(state.getBlock());
					if (fluid != null && Arrays.binarySearch(fluidList, fluid.getName()) >= 0) {
						continue;
					}

					fluid = Utils.lookupFluidForBlock(world.getBlockState(new BlockPos(x, y + 1, z)).getBlock());
					if (fluid == null || Arrays.binarySearch(fluidList, fluid.getName()) < 0) {
						continue;
					}
				}
				for (WeightedRandomBlock mat : matList) {
					if (state.getBlock().isReplaceableOreGen(state, world, new BlockPos(x, y, z), BlockMatcher.forBlock(mat.block))) {
						break l;
					}
				}
			} while (y-- > 1);

			if (y > 0) {
				generated |= f.applyGenerator(world, random, new BlockPos(x, y, z));
			}
		}
		return generated;
	}

	@Override
	public List<WeightedRandomBlock> defaultMaterials() {
		return Arrays.asList(new WeightedRandomBlock(Blocks.DIRT, -1), new WeightedRandomBlock(Blocks.GRASS, -1));
	}

	public static class Parser implements IDistributionParser {

		@Override
		public IDistribution parse(String name, Config genObject, Logger log) {

			boolean water = true;
			Set<String> fluidList = new HashSet<>();
			l:
			if (genObject.hasPath("fluid")) {
				ArrayList<DungeonHooks.DungeonMob> list = new ArrayList<>();
				if (!FeatureParser.parseWeightedStringList(genObject.root().get("fluid"), list)) {
					break l;
				}
				water = false;
				for (DungeonHooks.DungeonMob str : list) {
					// ints.add(FluidRegistry.getFluidID(str.type));
					// NOPE. this NPEs.
					Fluid fluid = FluidRegistry.getFluid(str.type.getResourcePath());
					if (fluid != null) {
						fluidList.add(fluid.getName());
					}
				}
			}

			List<WeightedRandomBlock> defaultMats = Arrays.asList(new WeightedRandomBlock(Blocks.DIRT, -1),
					new WeightedRandomBlock(Blocks.GRASS, -1));

			// TODO: WorldGeneratorAdv that allows access to its material list
			List<WeightedRandomBlock> matList = defaultMats;
			if (genObject.hasPath("material")) {
				matList = new ArrayList<>();
				if (!FeatureParser.parseResList(genObject.root().get("material"), matList, false)) {
					log.warn("Invalid material list! Using default list.");
					matList = defaultMats;
				}
			}

			return new FeatureGenUnderfluid(matList, fluidList.toArray(new String[0]));
		}
	}
}
