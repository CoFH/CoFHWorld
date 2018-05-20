package cofh.cofhworld.world.distribution;

import cofh.cofhworld.util.Utils;
import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.data.numbers.INumberProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fluids.Fluid;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DistributionUnderfluid extends Distribution {

	private final boolean water;
	private final WorldGenerator worldGen;
	private final INumberProvider count;
	private final List<WeightedRandomBlock> matList;
	private final String[] fluidList;

	public DistributionUnderfluid(String name, WorldGenerator worldGen, List<WeightedRandomBlock> matList, INumberProvider count, GenRestriction biomeRes, boolean regen, GenRestriction dimRes) {

		super(name, biomeRes, regen, dimRes);
		this.worldGen = worldGen;
		this.count = count;
		this.matList = matList;
		water = true;
		fluidList = null;
	}

	public DistributionUnderfluid(String name, WorldGenerator worldGen, List<WeightedRandomBlock> matList, String[] fluidList, INumberProvider count, GenRestriction biomeRes, boolean regen, GenRestriction dimRes) {

		super(name, biomeRes, regen, dimRes);
		this.worldGen = worldGen;
		this.count = count;
		this.matList = matList;
		water = false;
		this.fluidList = fluidList;
	}

	@Override
	public boolean generateFeature(Random random, int blockX, int blockZ, World world) {

		BlockPos pos = new BlockPos(blockX, 64, blockZ);

		final int count = this.count.intValue(world, random, pos);

		boolean generated = false;
		for (int i = 0; i < count; i++) {
			int x = blockX + random.nextInt(16);
			int z = blockZ + random.nextInt(16);
			if (!canGenerateInBiome(world, x, z, random)) {
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
				generated |= worldGen.generate(world, random, new BlockPos(x, y, z));
			}
		}
		return generated;
	}

}
