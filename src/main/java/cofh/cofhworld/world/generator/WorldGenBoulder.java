package cofh.cofhworld.world.generator;

import cofh.cofhworld.feature.Feature;
import cofh.cofhworld.feature.IGenerator;
import cofh.cofhworld.feature.IGeneratorParser;
import cofh.cofhworld.util.WeightedRandomBlock;
import com.typesafe.config.Config;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

public class WorldGenBoulder implements IGenerator {

	private final List<WeightedRandomBlock> cluster;
	private final WeightedRandomBlock[] genBlock;
	private final int size;
	public int sizeVariance = 2;
	public int clusters = 3;
	public int clusterVariance = 0;
	public boolean hollow = false;
	public float hollowAmt = 0.1665f;
	public float hollowVar = 0;

	public WorldGenBoulder(List<WeightedRandomBlock> resource, int minSize, List<WeightedRandomBlock> block) {

		cluster = resource;
		size = minSize;
		genBlock = block.toArray(new WeightedRandomBlock[block.size()]);
	}

	@Override
	public boolean generate(Feature feature, World world, Random rand, BlockPos pos) {

		int xCenter = pos.getX();
		int yCenter = pos.getY();
		int zCenter = pos.getZ();
		final int minSize = size, var = sizeVariance;
		boolean r = false;
		int i = clusterVariance > 0 ? clusters + rand.nextInt(clusterVariance + 1) : clusters;
		while (i-- > 0) {

			while (yCenter > minSize && world.isAirBlock(new BlockPos(xCenter, yCenter - 1, zCenter))) {
				--yCenter;
			}
			if (yCenter <= (minSize + var + 1)) {
				return false;
			}

			if (WorldGenMinableCluster.canGenerateInBlock(world, xCenter, yCenter - 1, zCenter, genBlock)) {

				int xWidth = minSize + (var > 1 ? rand.nextInt(var) : 0);
				int yWidth = minSize + (var > 1 ? rand.nextInt(var) : 0);
				int zWidth = minSize + (var > 1 ? rand.nextInt(var) : 0);
				float maxDist = (xWidth + yWidth + zWidth) * 0.333F + 0.5F;
				maxDist *= maxDist;
				float minDist = hollow ? (xWidth + yWidth + zWidth) * (hollowAmt * (1 - rand.nextFloat() * hollowVar)) : 0;
				minDist *= minDist;

				for (int x = -xWidth; x <= xWidth; ++x) {
					final int xDist = x * x;

					for (int z = -zWidth; z <= zWidth; ++z) {
						final int xzDist = xDist + z * z;

						for (int y = -yWidth; y <= yWidth; ++y) {
							final int dist = xzDist + y * y;

							if (dist <= maxDist) {
								if (dist >= minDist) {
									r |= WorldGenMinableCluster.generateBlock(world, xCenter + x, yCenter + y, zCenter + z, cluster);
								} else {
									r |= world.setBlockToAir(new BlockPos(xCenter + x, yCenter + y, zCenter + z));
								}
							}
						}
					}
				}
			}

			xCenter += rand.nextInt(var + minSize * 2) - (minSize + var / 2);
			zCenter += rand.nextInt(var + minSize * 2) - (minSize + var / 2);
			yCenter += rand.nextInt((var + 1) * 3) - (var + 1);
		}

		return r;
	}

	public static class Parser implements IGeneratorParser {

		@Override
		public IGenerator parseGenerator(String name, Config genObject, Logger log, List<WeightedRandomBlock> resList, List<WeightedRandomBlock> matList) {
			int clusterSize = genObject.getInt("diameter");
			if (clusterSize <= 0) {
				log.warn("Invalid diameter for generator '{}'", name);
				return null;
			}

			WorldGenBoulder r = new WorldGenBoulder(resList, clusterSize, matList);
			{
				if (genObject.hasPath("size-variance")) {
					r.sizeVariance = genObject.getInt("size-variance");
				}
				if (genObject.hasPath("count")) {
					r.clusters = genObject.getInt("count");
				}
				if (genObject.hasPath("count-variance")) {
					r.clusterVariance = genObject.getInt("count-variance");
				}
				if (genObject.hasPath("hollow")) {
					r.hollow = genObject.getBoolean("hollow");
				}
				if (genObject.hasPath("hollow-size")) {
					r.hollowAmt = (float) genObject.getDouble("hollow-size");
				}
				if (genObject.hasPath("hollow-variance")) {
					r.hollowVar = (float) genObject.getDouble("hollow-variance");
				}
			}
			return r;
		}
	}
}
