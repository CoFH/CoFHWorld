package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

public class WorldGenSparseMinableCluster extends WorldGen {

	private final List<WeightedBlock> resource;
	private final Material[] material;
	private final INumberProvider clusterSize;

	public WorldGenSparseMinableCluster(List<WeightedBlock> resource, INumberProvider clusterSize, List<Material> block) {

		this.resource = resource;
		this.clusterSize = clusterSize;
		material = block.toArray(new Material[0]);
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

}
