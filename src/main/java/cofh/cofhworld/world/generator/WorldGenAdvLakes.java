package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @deprecated TODO: replace all booleans with ICondition
 */
@Deprecated
public class WorldGenAdvLakes extends WorldGen {

	private static final List<WeightedBlock> GAP_BLOCK = Arrays.asList(new WeightedBlock(Blocks.AIR, 0));
	private final List<WeightedBlock> cluster;
	private final WeightedBlock[] genBlock;
	private List<WeightedBlock> outlineBlock = null;
	private List<WeightedBlock> gapBlock = GAP_BLOCK;
	private boolean solidOutline = false;
	private boolean totalOutline = false;
	private INumberProvider width;
	private INumberProvider height;

	public WorldGenAdvLakes(List<WeightedBlock> resource, List<WeightedBlock> block) {

		cluster = resource;
		if (block == null) {
			genBlock = null;
		} else {
			genBlock = block.toArray(new WeightedBlock[block.size()]);
		}
		this.setWidth(16);
		this.setHeight(9);
	}

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {

		int xStart = pos.getX();
		int yStart = pos.getY();
		int zStart = pos.getZ();

		DataHolder data = new DataHolder(pos);

		final int width = this.width.intValue(world, rand, data);
		final int height = this.height.intValue(world, rand, data);

		int widthOff = width / 2;
		int heightOff = height / 2 + 1;

		xStart -= widthOff;
		zStart -= widthOff;

		while (yStart > heightOff && world.isAirBlock(new BlockPos(xStart, yStart, zStart))) {
			--yStart;
		}
		--heightOff;
		if (yStart <= heightOff) {
			return false;
		}

		yStart -= heightOff;
		boolean[] spawnBlock = new boolean[width * width * height];

		int W = width - 1, H = height - 1;

		for (int i = 0, e = rand.nextInt(4) + 4; i < e; ++i) {
			double xSize = rand.nextDouble() * 6.0D + 3.0D;
			double ySize = rand.nextDouble() * 4.0D + 2.0D;
			double zSize = rand.nextDouble() * 6.0D + 3.0D;
			double xCenter = rand.nextDouble() * (width - xSize - 2.0D) + 1.0D + xSize / 2.0D;
			double yCenter = rand.nextDouble() * (height - ySize - 4.0D) + 2.0D + ySize / 2.0D;
			double zCenter = rand.nextDouble() * (width - zSize - 2.0D) + 1.0D + zSize / 2.0D;

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
					boolean flag = spawnBlock[(x * width + z) * height + y] || ((x < W && spawnBlock[((x + 1) * width + z) * height + y]) || (x > 0 && spawnBlock[((x - 1) * width + z) * height + y]) || (z < W && spawnBlock[(x * width + (z + 1)) * height + y]) || (z > 0 && spawnBlock[(x * width + (z - 1)) * height + y]) || (y < H && spawnBlock[(x * width + z) * height + (y + 1)]) || (y > 0 && spawnBlock[(x * width + z) * height + (y - 1)]));

					if (flag) {
						if (y >= heightOff) {
							Material material = world.getBlockState(new BlockPos(xStart + x, yStart + y, zStart + z)).getMaterial();
							if (material.isLiquid()) {
								return false;
							}
						} else {
							if (!canGenerateInBlock(world, xStart + x, yStart + y, zStart + z, genBlock)) {
								return false;
							}
						}
					}
				}
			}
		}

		for (x = 0; x < width; ++x) {
			for (z = 0; z < width; ++z) {
				for (y = 0; y < height; ++y) {
					if (spawnBlock[(x * width + z) * height + y]) {
						if (y < heightOff) {
							generateBlock(world, rand, xStart + x, yStart + y, zStart + z, genBlock, cluster);
						} else if (canGenerateInBlock(world, xStart + x, yStart + y, zStart + z, genBlock)) {
							generateBlock(world, rand, xStart + x, yStart + y, zStart + z, gapBlock);
						}
					}
				}
			}
		}

		for (x = 0; x < width; ++x) {
			for (z = 0; z < width; ++z) {
				for (y = 0; y < height; ++y) {
					if (spawnBlock[(x * width + z) * height + y] && world.getBlockState(new BlockPos(xStart + x, yStart + y - 1, zStart + z)).getBlock().equals(Blocks.DIRT) && world.getLightFor(EnumSkyBlock.SKY, new BlockPos(xStart + x, yStart + y, zStart + z)) > 0) {
						Biome bgb = world.getBiome(new BlockPos(xStart + x, 0, zStart + z));
						world.setBlockState(new BlockPos(xStart + x, yStart + y - 1, zStart + z), bgb.topBlock, 2);
					}
				}
			}
		}

		if (outlineBlock != null) {
			for (x = 0; x < width; ++x) {
				for (z = 0; z < width; ++z) {
					for (y = 0; y < height; ++y) {
						boolean flag = !spawnBlock[(x * width + z) * height + y] && ((x < W && spawnBlock[((x + 1) * width + z) * height + y]) || (x > 0 && spawnBlock[((x - 1) * width + z) * height + y]) || (z < W && spawnBlock[(x * width + (z + 1)) * height + y]) || (z > 0 && spawnBlock[(x * width + (z - 1)) * height + y]) || (y < H && spawnBlock[(x * width + z) * height + (y + 1)]) || (y > 0 && spawnBlock[(x * width + z) * height + (y - 1)]));

						if (flag && (solidOutline | y < heightOff || rand.nextInt(2) != 0) && (totalOutline || world.getBlockState(new BlockPos(xStart + x, yStart + y, zStart + z)).getMaterial().isSolid())) {
							generateBlock(world, rand, xStart + x, yStart + y, zStart + z, outlineBlock);
						}
					}
				}
			}
		}

		return true;
	}

	public WorldGenAdvLakes setWidth(int width) {

		this.width = new ConstantProvider(width);
		return this;
	}

	public WorldGenAdvLakes setWidth(INumberProvider width) {

		this.width = width;
		return this;
	}

	public WorldGenAdvLakes setHeight(int height) {

		this.height = new ConstantProvider(height);
		return this;
	}

	public WorldGenAdvLakes setHeight(INumberProvider height) {

		this.height = height;
		return this;
	}

	public WorldGenAdvLakes setSolidOutline(boolean outline) {

		this.solidOutline = outline;
		return this;
	}

	public WorldGenAdvLakes setTotalOutline(boolean outline) {

		this.totalOutline = outline;
		return this;
	}

	public WorldGenAdvLakes setOutlineBlock(List<WeightedBlock> blocks) {

		this.outlineBlock = blocks;
		return this;
	}

	public WorldGenAdvLakes setGapBlock(List<WeightedBlock> blocks) {

		this.gapBlock = blocks;
		return this;
	}

}
