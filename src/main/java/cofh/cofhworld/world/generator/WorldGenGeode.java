package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ConstantCondition;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WorldGenGeode extends WorldGen {

	private final List<WeightedBlock> resource;
	private final List<WeightedBlock> outline;
	private final Material[] material;
	private List<WeightedBlock> filler;
	private ICondition hollow;
	private INumberProvider width;
	private INumberProvider height;

	public WorldGenGeode(List<WeightedBlock> resource, List<Material> materials, List<WeightedBlock> cover) {

		this.resource = resource;
		material = materials.toArray(new Material[0]);
		outline = cover;
		filler = Collections.singletonList(WeightedBlock.AIR);
		hollow = ConstantCondition.FALSE;
		this.setWidth(16);
		this.setHeight(8);
	}

	@Override
	public boolean generate(IWorld world, Random rand, BlockPos pos) {

		int xStart = pos.getX();
		int yStart = pos.getY();
		int zStart = pos.getZ();

		DataHolder data = new DataHolder(pos);

		final int height = this.height.intValue(world, rand, data);
		final int width = this.width.intValue(world, rand, data);

		int heightOff = height / 2;
		int widthOff = width / 2;
		xStart -= widthOff;
		zStart -= widthOff;

		if (yStart <= heightOff) {
			return false;
		}

		yStart -= heightOff;
		boolean[] spawnBlock = new boolean[width * width * height];
		final boolean hollow = this.hollow.checkCondition(world, rand, data);
		boolean[] hollowBlock = hollow ? spawnBlock : new boolean[width * width * height];

		int W = width - 1, H = height - 1;

		for (int i = 0, e = rand.nextInt(4) + 4; i < e; ++i) {
			double xSize = rand.nextDouble() * 6.0D + 3.0D;
			double ySize = rand.nextDouble() * 4.0D + 2.0D;
			double zSize = rand.nextDouble() * 6.0D + 3.0D;
			double xCenter = rand.nextDouble() * (width - xSize - 2.0D) + 1.0D + xSize / 2.0D;
			double yCenter = rand.nextDouble() * (height - ySize - 4.0D) + 2.0D + ySize / 2.0D;
			double zCenter = rand.nextDouble() * (width - zSize - 2.0D) + 1.0D + zSize / 2.0D;
			double minDist = hollow ? rand.nextGaussian() * 0.15 + 0.4 : 0;

			for (int x = 1; x < W; ++x) {
				for (int z = 1; z < W; ++z) {
					for (int y = 1; y < H; ++y) {
						double xDist = (x - xCenter) / (xSize / 2.0D);
						double yDist = (y - yCenter) / (ySize / 2.0D);
						double zDist = (z - zCenter) / (zSize / 2.0D);
						double dist = xDist * xDist + yDist * yDist + zDist * zDist;

						if (dist < 1.0D) {
							spawnBlock[(x * width + z) * height + y] = true;
						}
						if (hollow && dist <= minDist) {
							hollowBlock[(x * width + z) * height + y] = true;
						}
					}
				}
			}
		}

		int x;
		int y;
		int z;

		for (x = 0; x < width; ++x) {
			for (z = 0; z < width; ++z) {
				for (y = 0; y < height; ++y) {
					boolean flag = (hollow && hollowBlock[(x * width + z) * height + y]) || spawnBlock[(x * width + z) * height + y] || ((x < W && spawnBlock[((x + 1) * width + z) * height + y]) || (x > 0 && spawnBlock[((x - 1) * width + z) * height + y]) || (z < W && spawnBlock[(x * width + (z + 1)) * height + y]) || (z > 0 && spawnBlock[(x * width + (z - 1)) * height + y]) || (y < H && spawnBlock[(x * width + z) * height + (y + 1)]) || (y > 0 && spawnBlock[(x * width + z) * height + (y - 1)]));

					if (flag && !canGenerateInBlock(world, xStart + x, yStart + y, zStart + z, material)) {
						return false;
					}
				}
			}
		}

		boolean r = false;
		for (x = 0; x < width; ++x) {
			for (z = 0; z < width; ++z) {
				for (y = 0; y < height; ++y) {
					if (spawnBlock[(x * width + z) * height + y]) {
						boolean t = generateBlock(world, rand, xStart + x, yStart + y, zStart + z, resource);
						r |= t;
						if (!t) {
							spawnBlock[(x * width + z) * height + y] = false;
						}
					}
				}
			}
		}

		for (x = 0; x < width; ++x) {
			for (z = 0; z < width; ++z) {
				for (y = 0; y < height; ++y) {
					if (hollow && hollowBlock[(x * width + z) * height + y]) {
						r |= generateBlock(world, rand, xStart + x, yStart + y, zStart + z, filler);
					} else {
						boolean flag = !spawnBlock[(x * width + z) * height + y] && ((x < W && spawnBlock[((x + 1) * width + z) * height + y]) || (x > 0 && spawnBlock[((x - 1) * width + z) * height + y]) || (z < W && spawnBlock[(x * width + (z + 1)) * height + y]) || (z > 0 && spawnBlock[(x * width + (z - 1)) * height + y]) || (y < H && spawnBlock[(x * width + z) * height + (y + 1)]) || (y > 0 && spawnBlock[(x * width + z) * height + (y - 1)]));

						if (flag) {
							r |= generateBlock(world, rand, xStart + x, yStart + y, zStart + z, outline);
						}
					}
				}
			}
		}

		return r;
	}

	public WorldGenGeode setWidth(int width) {

		this.width = new ConstantProvider(width);
		return this;
	}

	public WorldGenGeode setWidth(INumberProvider width) {

		this.width = width;
		return this;
	}

	public WorldGenGeode setHeight(int height) {

		this.height = new ConstantProvider(height);
		return this;
	}

	public WorldGenGeode setHeight(INumberProvider height) {

		this.height = height;
		return this;
	}

	public WorldGenGeode setHollow(ICondition hollow) {

		this.hollow = hollow;
		return this;
	}

	public WorldGenGeode setFillBlock(List<WeightedBlock> blocks) {

		this.filler = blocks;
		return this;
	}

}
