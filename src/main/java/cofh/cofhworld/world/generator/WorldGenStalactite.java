package cofh.cofhworld.world.generator;

import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * @deprecated TODO: replace all ints with INumberProvider
 */
@Deprecated
public class WorldGenStalactite extends WorldGenStalagmite {

	public WorldGenStalactite(List<WeightedBlock> resource, List<WeightedBlock> block, List<WeightedBlock> gblock) {

		super(resource, block, gblock);
	}

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {

		int xStart = pos.getX();
		int yStart = pos.getY();
		int zStart = pos.getZ();

		int end = world.getActualHeight();
		while (world.isAirBlock(new BlockPos(xStart, yStart, zStart)) && yStart < end) {
			++yStart;
		}

		if (!WorldGenMinableCluster.canGenerateInBlock(world, xStart, yStart--, zStart, baseBlock)) {
			return false;
		}

		int maxHeight = rand.nextInt(heightVariance) + minHeight;

		int size = genSize > 0 ? genSize : maxHeight / heightMod + rand.nextInt(sizeVariance);
		boolean r = false;
		for (int x = -size; x <= size; ++x) {
			for (int z = -size; z <= size; ++z) {
				if (!WorldGenMinableCluster.canGenerateInBlock(world, xStart + x, yStart + 1, zStart + z, baseBlock)) {
					continue;
				}
				int height = getHeight(x, z, size, rand, maxHeight);
				for (int y = 0; y < height; ++y) {
					r |= WorldGenMinableCluster.generateBlock(world, rand, xStart + x, yStart - y, zStart + z, genBlock, cluster);
				}
			}
		}
		return r;
	}
}
