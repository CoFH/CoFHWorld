package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

/**
 * @deprecated TODO: replace all ints with INumberProvider
 */
@Deprecated
public class WorldGenBoulder extends WorldGen {

	private final List<WeightedBlock> resource;
	private final Material[] material;
	private final int size;
	public int sizeVariance = 2;
	public int clusters = 3;
	public int clusterVariance = 0;
	public boolean hollow = false;
	public float hollowAmt = 0.1665f;
	public float hollowVar = 0;

	// TODO: shapes? sphere, cube, ellipsoid? more?

	public WorldGenBoulder(List<WeightedBlock> resource, int minSize, List<Material> materials) {

		this.resource = resource;
		size = minSize;
		material = materials.toArray(new Material[0]);
	}

	@Override
	public boolean generate(IWorld world, Random rand, BlockPos pos) {

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

			if (canGenerateInBlock(world, xCenter, yCenter - 1, zCenter, material)) {

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
									r |= generateBlock(world, rand, xCenter + x, yCenter + y, zCenter + z, resource);
								} else {
									r |= world.setBlockState(new BlockPos(xCenter + x, yCenter + y, zCenter + z), Blocks.AIR.getDefaultState(), 2 | 16);
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

}
