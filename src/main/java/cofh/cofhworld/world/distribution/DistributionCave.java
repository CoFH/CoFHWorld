package cofh.cofhworld.world.distribution;

import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.world.WorldValueProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public class DistributionCave extends Distribution {

	private final static INumberProvider GROUND_LEVEL = new WorldValueProvider("GROUND_LEVEL");

	private final WorldGenerator worldGen;
	private final INumberProvider count;
	private INumberProvider groundLevel = GROUND_LEVEL;
	private final boolean ceiling;

	public DistributionCave(String name, WorldGenerator worldGen, boolean ceiling, INumberProvider count, boolean regen) {

		super(name, regen);
		this.worldGen = worldGen;
		this.count = count;
		this.ceiling = ceiling;
	}

	public void setGroundLevel(INumberProvider level) {

		this.groundLevel = level;
	}

	@Override
	public boolean generateFeature(Random random, int blockX, int blockZ, World world) {

		BlockPos pos = new BlockPos(blockX, 64, blockZ);

		INumberProvider.DataHolder data = new INumberProvider.DataHolder(pos);

		final int count = this.count.intValue(world, random, data);

		worldGen.setDecorationDefaults();

		boolean generated = false;
		for (int i = 0; i < count; i++) {
			int x = blockX + random.nextInt(16);
			int z = blockZ + random.nextInt(16);
			if (!canGenerateInBiome(world, x, z, random)) {
				continue;
			}
			int seaLevel = groundLevel.intValue(world, random, data.setPosition(new BlockPos(x, 64, z)));
			if (seaLevel < 20 && groundLevel == GROUND_LEVEL) {
				seaLevel = world.getHeight();
			}

			int stopY = random.nextInt(1 + seaLevel / 2);
			int y = stopY;
			IBlockState state;
			do {
				state = world.getBlockState(new BlockPos(x, y, z));
			} while (!state.getBlock().isAir(state, world, new BlockPos(x, y, z)) && ++y < seaLevel);

			if (y == seaLevel) {
				y = 0;
				do {
					state = world.getBlockState(new BlockPos(x, y, z));
				} while (!state.getBlock().isAir(state, world, new BlockPos(x, y, z)) && ++y < stopY);
				if (y == stopY) {
					continue;
				}
			}

			if (ceiling) {
				if (y < stopY) {
					seaLevel = stopY + 1;
				}
				do {
					++y;
					state = world.getBlockState(new BlockPos(x, y, z));
				} while (y < seaLevel && state.getBlock().isAir(state, world, new BlockPos(x, y, z)));
				if (y == seaLevel) {
					continue;
				}
			} else if (state.getBlock().isAir(state, world, new BlockPos(x, y - 1, z))) {
				--y;
				do {
					state = world.getBlockState(new BlockPos(x, y, z));
				} while (state.getBlock().isAir(state, world, new BlockPos(x, y, z)) && y-- > 0);
				if (y == -1) {
					continue;
				}
			}

			generated |= worldGen.generate(world, random, new BlockPos(x, y, z));
		}
		return generated;
	}

}
