package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

public class WorldGenMinableCluster extends WorldGen {

	private final List<WeightedBlock> cluster;
	private final INumberProvider genClusterSize;
	private final WeightedBlock[] genBlock;

	public WorldGenMinableCluster(WeightedBlock resource, int clusterSize) {

		this(fabricateList(resource), clusterSize);
	}

	public WorldGenMinableCluster(List<WeightedBlock> resource, int clusterSize) {

		this(resource, clusterSize, Blocks.STONE);
	}

	public WorldGenMinableCluster(WeightedBlock resource, int clusterSize, Block block) {

		this(fabricateList(resource), clusterSize, block);
	}

	public WorldGenMinableCluster(List<WeightedBlock> resource, int clusterSize, Block block) {

		this(resource, clusterSize, fabricateList(block));
	}

	public WorldGenMinableCluster(List<WeightedBlock> resource, int clusterSize, List<WeightedBlock> block) {

		this(resource, new ConstantProvider(clusterSize), block);
	}

	public WorldGenMinableCluster(List<WeightedBlock> resource, INumberProvider clusterSize, List<WeightedBlock> block) {

		cluster = resource;
		genClusterSize = clusterSize;
		genBlock = block.toArray(new WeightedBlock[0]);
	}

	@Override
	public boolean generate(IWorld world, Random rand, BlockPos pos) {

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		int blocks = MathHelper.clamp(genClusterSize.intValue(world, rand, new DataHolder(pos)), 1, 42);
		if (blocks < 4) { // HACK: at 1 and 2 no ores are ever generated. at 3 only 1/3 veins generate
			return generateTiny(world, rand, blocks, x, y, z);
		}
		float f = rand.nextFloat() * (float) Math.PI;
		// despite naming, these are not exactly min/max. more like direction
		float xMin = x + (MathHelper.sin(f) * blocks) / 8F;
		float xMax = x - (MathHelper.sin(f) * blocks) / 8F;
		float zMin = z + (MathHelper.cos(f) * blocks) / 8F;
		float zMax = z - (MathHelper.cos(f) * blocks) / 8F;
		float yMin = (y + rand.nextInt(3)) - 2;
		float yMax = (y + rand.nextInt(3)) - 2;

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
			float size = ((float) rand.nextDouble() * blocks) / 16F;

			float hMod = ((MathHelper.sin((i * (float) Math.PI) / blocks) + 1F) * size + 1F) * 0.5F;
			float vMod = ((MathHelper.sin((i * (float) Math.PI) / blocks) + 1F) * size + 1F) * 0.5F;

			int xStart = MathHelper.floor(xCenter - hMod);
			int yStart = MathHelper.floor(yCenter - vMod);
			int zStart = MathHelper.floor(zCenter - hMod);

			int xStop = MathHelper.floor(xCenter + hMod);
			int yStop = MathHelper.floor(yCenter + vMod);
			int zStop = MathHelper.floor(zCenter + hMod);

			for (int blockX = xStart; blockX <= xStop; blockX++) {
				float xDistSq = ((blockX + .5F) - xCenter) / hMod;
				xDistSq *= xDistSq;
				if (xDistSq >= 1F) {
					continue;
				}
				for (int blockY = yStart; blockY <= yStop; blockY++) {
					float yDistSq = ((blockY + .5F) - yCenter) / vMod;
					yDistSq *= yDistSq;
					float xyDistSq = yDistSq + xDistSq;
					if (xyDistSq >= 1F) {
						continue;
					}
					for (int blockZ = zStart; blockZ <= zStop; blockZ++) {
						float zDistSq = ((blockZ + .5F) - zCenter) / hMod;
						zDistSq *= zDistSq;
						if (zDistSq + xyDistSq >= 1F) {
							continue;
						}
						r |= generateBlock(world, rand, blockX, blockY, blockZ, genBlock, cluster);
					}
				}
			}
		}
		return r;
	}

	public boolean generateTiny(IWorld world, Random random, int clusterSize, int x, int y, int z) {

		boolean r = generateBlock(world, random, x, y, z, genBlock, cluster);
		// not <=; generating up to clusterSize blocks
		for (int i = 1; i < clusterSize; i++) {
			int d0 = x + random.nextInt(2);
			int d1 = y + random.nextInt(2);
			int d2 = z + random.nextInt(2);

			r |= generateBlock(world, random, d0, d1, d2, genBlock, cluster);
		}
		return r;
	}

}
