package cofh.cofhworld.world.distribution;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.util.Utils;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.pattern.BlockMatcher;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DistributionUnderfluid extends Distribution {

	private final boolean water;
	private final WorldGen worldGen;
	private final INumberProvider count;
	private final List<WeightedBlock> matList;
	private final ResourceLocation[] fluidList;

	public DistributionUnderfluid(String name, WorldGen worldGen, List<WeightedBlock> matList, INumberProvider count, boolean regen) {

		super(name, regen);
		this.worldGen = worldGen;
		this.count = count;
		this.matList = matList;
		water = true;
		fluidList = null;
	}

	public DistributionUnderfluid(String name, WorldGen worldGen, List<WeightedBlock> matList, String[] fluidList, INumberProvider count, boolean regen) {

		super(name, regen);
		this.worldGen = worldGen;
		this.count = count;
		this.matList = matList;
		water = false;
		this.fluidList = Arrays.stream(fluidList).distinct().map(ResourceLocation::new).sorted().toArray(ResourceLocation[]::new);
	}

	@Override
	public boolean generateFeature(Random random, int blockX, int blockZ, IWorld world) {

		BlockPos pos = new BlockPos(blockX, 64, blockZ);

		final int count = this.count.intValue(world, random, new DataHolder(pos));

		worldGen.setDecorationDefaults();

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
				BlockState state = world.getBlockState(new BlockPos(x, y, z));
				if (water) {
					if (state.getMaterial() == Material.WATER) {
						continue;
					}
					if (world.getBlockState(new BlockPos(x, y + 1, z)).getMaterial() != Material.WATER) {
						continue;
					}
				} else {
					Fluid fluid = Utils.lookupFluidForBlock(state.getBlock());
					if (fluid != null && Arrays.binarySearch(fluidList, fluid.getRegistryName()) >= 0) {
						continue;
					}

					fluid = Utils.lookupFluidForBlock(world.getBlockState(new BlockPos(x, y + 1, z)).getBlock());
					if (fluid == null || Arrays.binarySearch(fluidList, fluid.getRegistryName()) < 0) {
						continue;
					}
				}
				for (WeightedBlock mat : matList) {
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
