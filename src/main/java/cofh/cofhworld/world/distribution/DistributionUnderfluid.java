package cofh.cofhworld.world.distribution;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.util.Utils;
import cofh.cofhworld.world.generator.WorldGen;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DistributionUnderfluid extends Distribution {

	private final boolean water;
	private final WorldGen worldGen;
	private final INumberProvider count;
	private final List<cofh.cofhworld.data.block.Material> matList;
	private final ResourceLocation[] fluidList;

	public DistributionUnderfluid(String name, WorldGen worldGen, List<cofh.cofhworld.data.block.Material> matList, INumberProvider count, boolean regen) {

		super(name, regen);
		this.worldGen = worldGen;
		this.count = count;
		this.matList = matList;
		water = true;
		fluidList = null;
	}

	public DistributionUnderfluid(String name, WorldGen worldGen, List<cofh.cofhworld.data.block.Material> matList, String[] fluidList, INumberProvider count, boolean regen) {

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
				pos = new BlockPos(x, y, z);
				BlockState state = world.getBlockState(pos);
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
				for (cofh.cofhworld.data.block.Material mat : matList) {
					if (state.isReplaceableOreGen(world, pos, mat)) {
						break l;
					}
				}
			} while (y-- > 1);

			if (y > 0) {
				generated |= worldGen.generate(world, random, pos);
			}
		}
		return generated;
	}

}
