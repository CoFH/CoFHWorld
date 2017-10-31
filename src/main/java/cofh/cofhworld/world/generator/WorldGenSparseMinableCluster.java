package cofh.cofhworld.world.generator;

import cofh.cofhworld.feature.Feature;
import cofh.cofhworld.feature.IGenerator;
import cofh.cofhworld.feature.IGeneratorParser;
import cofh.cofhworld.util.WeightedRandomBlock;
import cofh.cofhworld.util.numbers.ConstantProvider;
import cofh.cofhworld.util.numbers.INumberProvider;
import com.typesafe.config.Config;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

public class WorldGenSparseMinableCluster implements IGenerator {

	private final List<WeightedRandomBlock> cluster;
	private final INumberProvider genClusterSize;
	private final WeightedRandomBlock[] genBlock;

	public WorldGenSparseMinableCluster(List<WeightedRandomBlock> resource, int clusterSize, List<WeightedRandomBlock> block) {

		cluster = resource;
		genClusterSize = new ConstantProvider(clusterSize);
		genBlock = block.toArray(new WeightedRandomBlock[block.size()]);
	}

	@Override
	public boolean generate(Feature feature, World world, Random rand, BlockPos pos) {

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		int blocks = MathHelper.clamp(genClusterSize.intValue(world, rand, pos), 1, 42);
		float f = rand.nextFloat() * (float) Math.PI;
		// despite naming, these are not exactly min/max. more like direction
		float yMin = (y + rand.nextInt(3)) - 2;
		float yMax = (y + rand.nextInt(3)) - 2;
		// { HACK: at 1 and 2 no ores are ever generated. by doing it this way,
		// 3 = 1/3rd clusters gen, 2 = 1/6, 1 = 1/12 allowing for much finer
		// grained rarity than the non-sparse version
		if (blocks == 1 && yMin > yMax) {
			++blocks;
		}
		if (blocks == 2 && f > (float) Math.PI * 0.5f) {
			++blocks;
		}
		// }
		float xMin = x + (MathHelper.sin(f) * blocks) / 8F;
		float xMax = x - (MathHelper.sin(f) * blocks) / 8F;
		float zMin = z + (MathHelper.cos(f) * blocks) / 8F;
		float zMax = z - (MathHelper.cos(f) * blocks) / 8F;

		// optimization so this subtraction doesn't occur every time in the loop
		xMax -= xMin;
		yMax -= yMin;
		zMax -= zMin;

		boolean r = false;
		for (int i = 0; i <= blocks; i++) {

			float xCenter = xMin + (xMax * i) / blocks;
			float yCenter = yMin + (yMax * i) / blocks;
			float zCenter = zMin + (zMax * i) / blocks;

			// preserved as nextDouble to ensure the rand gets ticked the same amount
			float size = ((float) rand.nextDouble() * blocks) / 16f;

			float hMod = ((MathHelper.sin((i * (float) Math.PI) / blocks) + 1f) * size + 1f) * .5f;
			float vMod = ((MathHelper.sin((i * (float) Math.PI) / blocks) + 1f) * size + 1f) * .5f;

			int xStart = MathHelper.floor(xCenter - hMod);
			int yStart = MathHelper.floor(yCenter - vMod);
			int zStart = MathHelper.floor(zCenter - hMod);

			int xStop = MathHelper.floor(xCenter + hMod);
			int yStop = MathHelper.floor(yCenter + vMod);
			int zStop = MathHelper.floor(zCenter + hMod);

			for (int blockX = xStart; blockX <= xStop; blockX++) {
				float xDistSq = ((blockX + .5f) - xCenter) / hMod;
				xDistSq *= xDistSq;
				if (xDistSq >= 1f) {
					continue;
				}

				for (int blockY = yStart; blockY <= yStop; blockY++) {
					float yDistSq = ((blockY + .5f) - yCenter) / vMod;
					yDistSq *= yDistSq;
					float xyDistSq = yDistSq + xDistSq;
					if (xyDistSq >= 1f) {
						continue;
					}

					for (int blockZ = zStart; blockZ <= zStop; blockZ++) {
						float zDistSq = ((blockZ + .5f) - zCenter) / hMod;
						zDistSq *= zDistSq;
						if (zDistSq + xyDistSq >= 1f) {
							continue;
						}

						r |= WorldGenMinableCluster.generateBlock(world, blockX, blockY, blockZ, genBlock, cluster);
					}
				}
			}
		}

		return r;
	}

	public static class Parser implements IGeneratorParser {

		@Override
		public IGenerator parseGenerator(String name, Config genObject, Logger log, List<WeightedRandomBlock> resList, List<WeightedRandomBlock> matList) {
			int clusterSize = genObject.getInt("cluster-size");
			if (clusterSize <= 0) {
				log.warn("Invalid cluster size for generator '{}'", name);
				return null;
			}

			return new WorldGenSparseMinableCluster(resList, clusterSize, matList);
		}
	}
}
