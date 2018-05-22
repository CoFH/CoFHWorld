package cofh.cofhworld.world.generator;

import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.List;
import java.util.Random;

/**
 * @deprecated TODO: replace all ints with INumberProvider
 */
@Deprecated
public class WorldGenSpike extends WorldGenerator {

	private final List<WeightedBlock> cluster;
	private final WeightedBlock[] genBlock;
	public boolean largeSpikes = true;
	public int largeSpikeChance = 60;
	public int minHeight = 7;
	public int heightVariance = 4;
	public int sizeVariance = 2;
	public int positionVariance = 3;
	public int minLargeSpikeHeightGain = 10;
	public int largeSpikeHeightVariance = 30;
	public int largeSpikeFillerSize = 1;

	public WorldGenSpike(List<WeightedBlock> resource, List<WeightedBlock> block) {

		cluster = resource;
		genBlock = block.toArray(new WeightedBlock[block.size()]);
	}

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {

		int xStart = pos.getX();
		int yStart = pos.getY();
		int zStart = pos.getZ();

		while (world.isAirBlock(new BlockPos(xStart, yStart, zStart)) && yStart > 2) {
			--yStart;
		}

		if (!WorldGenMinableCluster.canGenerateInBlock(world, xStart, yStart, zStart, genBlock)) {
			return false;
		}

		int height = rand.nextInt(heightVariance) + minHeight, originalHeight = height;
		int size = height / (minHeight / 2) + rand.nextInt(sizeVariance);
		if (size > 1 && positionVariance > 0) {
			yStart += rand.nextInt(positionVariance + 1) - 1;
		}

		if (largeSpikes && size > 1 && (largeSpikeChance <= 0 || rand.nextInt(largeSpikeChance) == 0)) {
			height += minLargeSpikeHeightGain + rand.nextInt(largeSpikeHeightVariance);
		}

		int offsetHeight = height - originalHeight;

		for (int y = 0; y < height; ++y) {
			float layerSize;
			if (y >= offsetHeight) {
				layerSize = (1.0F - (float) (y - offsetHeight) / (float) originalHeight) * size;
			} else {
				layerSize = largeSpikeFillerSize;
			}
			int width = MathHelper.ceil(layerSize);

			for (int x = -width; x <= width; ++x) {
				float xDist = MathHelper.abs(x) - 0.25F;

				for (int z = -width; z <= width; ++z) {
					float zDist = MathHelper.abs(z) - 0.25F;

					if ((x == 0 && z == 0 || xDist * xDist + zDist * zDist <= layerSize * layerSize) && (x != -width && x != width && z != -width && z != width || rand.nextFloat() <= 0.75F)) {

						WorldGenMinableCluster.generateBlock(world, rand, xStart + x, yStart + y, zStart + z, genBlock, cluster);

						if (y != 0 && width > 1) {
							WorldGenMinableCluster.generateBlock(world, rand, xStart + x, yStart - y + offsetHeight, zStart + z, genBlock, cluster);
						}
					}
				}
			}
		}
		return true;
	}

}
