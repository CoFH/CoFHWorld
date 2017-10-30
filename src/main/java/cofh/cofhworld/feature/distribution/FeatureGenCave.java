package cofh.cofhworld.feature.distribution;

import cofh.cofhworld.feature.Feature;
import cofh.cofhworld.feature.IDistribution;
import cofh.cofhworld.feature.IDistributionParser;
import cofh.cofhworld.util.WeightedRandomBlock;
import com.typesafe.config.Config;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FeatureGenCave implements IDistribution {

	final boolean ceiling;

	public FeatureGenCave(boolean ceiling) {

		this.ceiling = ceiling;
	}

	@Override
	public boolean apply(Feature f, Random random, int blockX, int blockZ, World world) {
		int averageSeaLevel = world.provider.getAverageGroundLevel() + 1;

		BlockPos pos = new BlockPos(blockX, 64, blockZ);

		final int count = f.getChunkCount().intValue(world, random, pos);

		boolean generated = false;
		for (int i = 0; i < count; i++) {
			int x = blockX + random.nextInt(16);
			int z = blockZ + random.nextInt(16);
			if (!f.canGenerateInBiome(world, x, z, random)) {
				continue;
			}
			int seaLevel = averageSeaLevel;
			if (seaLevel < 20) {
				seaLevel = world.getHeight(x, z);
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

			generated |= f.applyGenerator(world, random, new BlockPos(x, y, z));
		}
		return generated;
	}

	@Override
	public List<WeightedRandomBlock> defaultMaterials() {
		return Arrays.asList(new WeightedRandomBlock(Blocks.STONE, -1));
	}

	public static class Parser implements IDistributionParser {

		@Override
		public IDistribution parse(String name, Config config, Logger log) {

			boolean ceiling = config.hasPath("ceiling") && config.getBoolean("ceiling");
			return new FeatureGenCave(ceiling);
		}
	}
}
