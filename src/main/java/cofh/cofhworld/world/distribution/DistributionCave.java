package cofh.cofhworld.world.distribution;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.data.DataProvider;
import cofh.cofhworld.data.numbers.operation.MathProvider;
import cofh.cofhworld.data.numbers.random.UniformRandomProvider;
import cofh.cofhworld.data.numbers.world.WorldValueProvider;
import cofh.cofhworld.world.generator.WorldGen;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;

import java.util.Random;

public class DistributionCave extends Distribution {

	private final static INumberProvider GROUND_LEVEL = new WorldValueProvider("GROUND_LEVEL");
	private final static INumberProvider INTERMEDIATE_LEVEL = new UniformRandomProvider(
			ConstantProvider.ZERO,
			new MathProvider(ConstantProvider.ONE, new DataProvider("ground-level"), "ADD")
	);

	private final WorldGen worldGen;
	private final INumberProvider count;
	private INumberProvider groundLevel = GROUND_LEVEL, intermediateLevel = INTERMEDIATE_LEVEL, floorLevel = ConstantProvider.ZERO; // TODO: names
	private final boolean ceiling;

	public DistributionCave(String name, WorldGen worldGen, boolean ceiling, INumberProvider count, boolean regen) {

		super(name, regen);
		this.worldGen = worldGen;
		this.count = count;
		this.ceiling = ceiling;
	}

	public void setGroundLevel(INumberProvider level) {

		this.groundLevel = level;
	}

	public void setIntermediateLevel(INumberProvider level) {

		this.intermediateLevel = level;
	}

	@Override
	public boolean generateFeature(Random random, int blockX, int blockZ, ISeedReader world) { // TODO: this logic needs looked at

		BlockPos pos = new BlockPos(blockX, 64, blockZ);

		DataHolder data = new DataHolder(pos);

		final int count = this.count.intValue(world, random, data);
		data.setValue("cluster-count", count);

		worldGen.setDecorationDefaults();

		boolean generated = false;
		for (int i = 0; i < count; i++) {
			data.setValue("cluster-current", i);
			int x = blockX + random.nextInt(16);
			int z = blockZ + random.nextInt(16);
			if (!canGenerateInBiome(world, x, z, random)) {
				continue;
			}
			int seaLevel = groundLevel.intValue(world, random, data.setPosition(new BlockPos(x, 64, z)));
			if (seaLevel < 20 && groundLevel == GROUND_LEVEL) {
				seaLevel = world.getHeight();
			}

			int stopY = intermediateLevel.intValue(world, random, data.setValue("ground-level", seaLevel)),
					minY = floorLevel.intValue(world, random,data.setValue("intermediate-level", stopY));
			int y = stopY;
			BlockState state;
			do {
				state = world.getBlockState(new BlockPos(x, y, z));
			} while (!state.getBlock().isAir(state, world, new BlockPos(x, y, z)) && ++y < seaLevel);

			if (y == seaLevel & stopY > minY) {
				y = minY;
				do {
					state = world.getBlockState(new BlockPos(x, y, z));
				} while (!state.getBlock().isAir(state, world, new BlockPos(x, y, z)) && ++y < stopY);
				if (y >= stopY) {
					continue;
				}
			}

			data.setValue("floor-level", minY);
			if (ceiling) {
				if (y < stopY) {
					seaLevel = stopY + 1;
				}
				do {
					++y;
					state = world.getBlockState(new BlockPos(x, y, z));
				} while (y < seaLevel && state.getBlock().isAir(state, world, new BlockPos(x, y, z)));
				if (y >= seaLevel) {
					continue;
				}
			} else if (state.getBlock().isAir(state, world, new BlockPos(x, y - 1, z))) {
				--y;
				do {
					state = world.getBlockState(new BlockPos(x, y, z));
				} while (state.getBlock().isAir(state, world, new BlockPos(x, y, z)) && y-- > minY);
				if (y <= minY) {
					continue;
				}
			}

			generated |= worldGen.generate(world, random, new BlockPos(x, y, z));
		}
		return generated;
	}

}
