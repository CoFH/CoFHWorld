package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

public class WorldGenCluster extends WorldGen {

	protected final List<WeightedBlock> resource;
	protected final Material[] material;
	protected final INumberProvider clusterSize;

	public WorldGenCluster(List<WeightedBlock> resource, INumberProvider clusterSize, List<Material> materials) {

		this.resource = resource;
		this.clusterSize = clusterSize;
		material = materials.toArray(new Material[0]);
	}

	@Override
	public boolean generate(IWorld world, Random rand, final DataHolder data) {

		int x = data.getPosition().getX();
		int y = data.getPosition().getY();
		int z = data.getPosition().getZ();

		int blocks = clusterSize.intValue(world, rand, data);
		float f = rand.nextFloat() * (float) Math.PI;
		// despite naming, these are not exactly min/max. more like direction
		float xMin = x + (MathHelper.sin(f) * blocks) / 8F;
		float xMax = x - (MathHelper.sin(f) * blocks) / 8F;
		float zMin = z + (MathHelper.cos(f) * blocks) / 8F;
		float zMax = z - (MathHelper.cos(f) * blocks) / 8F;
		float yMin = (y + rand.nextInt(3)) - 2;
		float yMax = (y + rand.nextInt(3)) - 2;

		return generateBody(world, rand, blocks, xMin, yMin, zMin, xMax, yMax, zMax);
	}

	protected boolean generateBody(IWorld world, Random rand, int blocks, float xMin, float yMin, float zMin, float xMax, float yMax, float zMax) {

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

						r |= generateBlock(world, rand, blockX, blockY, blockZ, material, resource);
					}
				}
			}
		}

		return r;
	}

	public static class Sparse extends WorldGenCluster {

		public Sparse(List<WeightedBlock> resource, INumberProvider clusterSize, List<Material> materials) {

			super(resource, clusterSize, materials);
		}

		@Override
		public boolean generate(IWorld world, Random rand, final DataHolder data) {

			int x = data.getPosition().getX();
			int y = data.getPosition().getY();
			int z = data.getPosition().getZ();

			int blocks = clusterSize.intValue(world, rand, data);
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

			return generateBody(world, rand, blocks, xMin, yMin, zMin, xMax, yMax, zMax);
		}

	}

	public static class Tiny extends WorldGenCluster {

		public Tiny(List<WeightedBlock> resource, INumberProvider clusterSize, List<Material> materials) {

			super(resource, clusterSize, materials);
		}

		@Override
		public boolean generate(IWorld world, Random rand, final DataHolder data) {

			int x = data.getPosition().getX();
			int y = data.getPosition().getY();
			int z = data.getPosition().getZ();

			int blocks = clusterSize.intValue(world, rand, data);
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

			return generateBody(world, rand, blocks, xMin, yMin, zMin, xMax, yMax, zMax);
		}

		public boolean generateTiny(IWorld world, Random random, int clusterSize, int x, int y, int z) {

			boolean r = generateBlock(world, random, x, y, z, material, resource);
			// not <=; generating up to clusterSize blocks
			for (int i = 1; i < clusterSize; i++) {
				int d0 = x + random.nextInt(2);
				int d1 = y + random.nextInt(2);
				int d2 = z + random.nextInt(2);

				r |= generateBlock(world, random, d0, d1, d2, material, resource);
			}
			return r;
		}

	}
}
