package cofh.cofhworld.world.generator;

import cofh.cofhworld.feature.Feature;
import cofh.cofhworld.feature.IGenerator;
import cofh.cofhworld.util.WeightedRandomBlock;
import com.typesafe.config.Config;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

public class StalactiteGen extends StalagmiteGen {

	public StalactiteGen(List<WeightedRandomBlock> resource, List<WeightedRandomBlock> block) {

		super(resource, block);
	}

	@Override
	public boolean generate(Feature feature, World world, Random rand, BlockPos pos) {

		int xStart = pos.getX();
		int yStart = pos.getY();
		int zStart = pos.getZ();

		int end = world.getActualHeight();
		while (world.isAirBlock(new BlockPos(xStart, yStart, zStart)) && yStart < end) {
			++yStart;
		}

		if (!ClusterGen.canGenerateInBlock(world, xStart, yStart--, zStart, baseBlock)) {
			return false;
		}

		int maxHeight = rand.nextInt(heightVariance) + minHeight;

		int size = genSize > 0 ? genSize : maxHeight / heightMod + rand.nextInt(sizeVariance);
		boolean r = false;
		for (int x = -size; x <= size; ++x) {
			for (int z = -size; z <= size; ++z) {
				if (!ClusterGen.canGenerateInBlock(world, xStart + x, yStart + 1, zStart + z, baseBlock)) {
					continue;
				}
				int height = getHeight(x, z, size, rand, maxHeight);
				for (int y = 0; y < height; ++y) {
					r |= ClusterGen.generateBlock(world, xStart + x, yStart - y, zStart + z, genBlock, cluster);
				}
			}
		}
		return r;
	}

	public static class Parser extends StalagmiteGen.Parser {
		@Override
		public IGenerator parseGenerator(String generatorName, Config genObject, Logger log, List<WeightedRandomBlock> resList, List<WeightedRandomBlock> matList) {

			return commonParse(new StalactiteGen(resList, matList), generatorName, genObject, log);
		}
	}
}
