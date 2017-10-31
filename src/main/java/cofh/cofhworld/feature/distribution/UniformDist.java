package cofh.cofhworld.feature.distribution;

import cofh.cofhworld.feature.Feature;
import cofh.cofhworld.feature.IDistribution;
import cofh.cofhworld.feature.IDistributionParser;
import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.util.numbers.INumberProvider;
import com.typesafe.config.Config;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class UniformDist implements IDistribution {

	final INumberProvider minY;
	final INumberProvider maxY;

	public UniformDist(INumberProvider minY, INumberProvider maxY) {

		this.minY = minY;
		this.maxY = maxY;
	}

	@Override
	public boolean apply(Feature f, Random random, int chunkX, int chunkZ, World world) {
		BlockPos pos = new BlockPos(chunkX, 64, chunkZ);

		final int count = f.getChunkCount().intValue(world, random, pos);
		final int minY = Math.max(this.minY.intValue(world, random, pos), 0), maxY = this.maxY.intValue(world, random, pos);
		if (minY > maxY) {
			return false;
		}

		boolean generated = false;
		for (int i = 0; i < count; i++) {
			int x = chunkX + random.nextInt(16);
			int y = minY + (minY != maxY ? random.nextInt(maxY - minY) : 0);
			int z = chunkZ + random.nextInt(16);
			if (!f.canGenerateInBiome(world, x, z, random)) {
				continue;
			}
			generated |= f.applyGenerator(world, random, new BlockPos(x,y,z));
		}
		return generated;
	}

	@Override
	public List<WeightedRandomBlock> defaultMaterials() {
		return Arrays.asList(new WeightedRandomBlock(Blocks.STONE, -1));
	}

	@Override
	public String defaultGenerator() {
		return "cluster";
	}

	public static class Parser implements IDistributionParser {

		@Override
		public IDistribution parse(String featureName, Config config, Logger log) {

			if (!(config.hasPath("min-height") && config.hasPath("max-height"))) {
				log.error("Height parameters for 'uniform' template not specified in \"" + featureName + "\"");
				return null;
			}

			INumberProvider minHeight = FeatureParser.parseNumberValue(config.root().get("min-height"));
			INumberProvider maxHeight = FeatureParser.parseNumberValue(config.root().get("max-height"));

			return new UniformDist(minHeight, maxHeight);
		}
	}

}
