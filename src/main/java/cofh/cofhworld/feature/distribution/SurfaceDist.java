package cofh.cofhworld.feature.distribution;

import cofh.cofhworld.feature.Feature;
import cofh.cofhworld.feature.IDistribution;
import cofh.cofhworld.feature.IDistributionParser;
import cofh.cofhworld.init.FeatureParser;
import cofh.cofhworld.util.Utils;
import cofh.cofhworld.util.WeightedRandomBlock;
import com.typesafe.config.Config;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static cofh.cofhworld.world.generator.ClusterGen.canGenerateInBlock;

public class SurfaceDist implements IDistribution {

	final WeightedRandomBlock[] matList;
	final boolean useTopBlock;

	public SurfaceDist(List<WeightedRandomBlock> matList, boolean useTopBlock) {

		this.matList = matList.toArray(new WeightedRandomBlock[matList.size()]);
		this.useTopBlock = useTopBlock;
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


			int y = useTopBlock ? Utils.getTopBlockY(world, x, z) : Utils.getSurfaceBlockY(world, x, z);
			l:
			{
				IBlockState state = world.getBlockState(new BlockPos(x, y, z));
				if (!state.getBlock().isAir(state, world, new BlockPos(x, y, z)) && canGenerateInBlock(world, x, y, z, matList)) {
					break l;
				}
				continue;
			}

			generated |= f.applyGenerator(world, random, new BlockPos(x, y + 1, z));
		}
		return generated;
	}

	@Override
	public List<WeightedRandomBlock> defaultMaterials() {
		return Arrays.asList(matList);
	}

	@Override
	public String defaultGenerator() {
		return "cluster";
	}

	public static class Parser implements IDistributionParser {

		@Override
		public IDistribution parse(String name, Config config, Logger log) {

			// The Surface distribution is unique in that it needs to check the materials config BEFORE the
			// generator(s) get invoked; the cleanest way (that I could find) to implement this was to
			// parse materials when instantiating the distribution. They get parsed AGAIN for the generator in
			// FeatureParser (blech).
			List<WeightedRandomBlock> defaultMats = Arrays.asList(new WeightedRandomBlock(Blocks.STONE, -1),
					new WeightedRandomBlock(Blocks.DIRT, -1), new WeightedRandomBlock(Blocks.GRASS, -1),
					new WeightedRandomBlock(Blocks.SAND, -1), new WeightedRandomBlock(Blocks.GRAVEL, -1),
					new WeightedRandomBlock(Blocks.SNOW, -1), new WeightedRandomBlock(Blocks.AIR, -1),
					new WeightedRandomBlock(Blocks.WATER, -1));

			List<WeightedRandomBlock> matList = defaultMats;
			if (config.hasPath("material")) {
				matList = new ArrayList<>();
				if (!FeatureParser.parseResList(config.root().get("material"), matList, false)) {
					log.warn("Invalid material list! Using default list.");
					matList = defaultMats;
				}
			}
			// TODO: clarity on follow-terrain field
			boolean useTopBlock = (config.hasPath("follow-terrain") && config.getBoolean("follow-terrain"));
			return new SurfaceDist(matList, useTopBlock);
		}
	}
}
