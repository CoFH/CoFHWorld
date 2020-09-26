package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.condition.world.WorldValueCondition;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.world.DirectionalScanner;
import cofh.cofhworld.data.numbers.world.WorldValueProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Random;

public class WorldGenAdvLakes extends WorldGen {

	private final Material[] material;
	private final List<WeightedBlock> resource;

	private final List<WeightedBlock> filler;

	private final List<WeightedBlock> outline;
	private final ICondition outlineCondition;

	private INumberProvider width;
	private INumberProvider height;

	public WorldGenAdvLakes(List<WeightedBlock> resource, List<Material> materials, List<WeightedBlock> filler, List<WeightedBlock> outline, ICondition outlineCondition) {

		this.resource = resource;
		this.filler = filler;
		this.outline = outline;
		this.outlineCondition = outlineCondition;
		if (materials == null) {
			material = null;
		} else {
			material = materials.toArray(new Material[0]);
		}
		setWidth(16);
		setHeight(8);

		setOffsetY(new DirectionalScanner(new WorldValueCondition("IS_AIR"), Direction.DOWN, new WorldValueProvider("CURRENT_Y")));
	}

	@Override
	public boolean generate(IWorld world, Random rand, final DataHolder data) {

		int xStart = data.getPosition().getX();
		int yStart = data.getPosition().getY();
		int zStart = data.getPosition().getZ();

		final int width = this.width.intValue(world, rand, data);
		final int height = this.height.intValue(world, rand, data.setValue("width", width));
		data.setValue("height", height);

		final int widthOff = width / 2;
		final int heightOff = height / 2;

		xStart -= widthOff;
		zStart -= widthOff;

		if (yStart <= heightOff) {
			return false;
		}

		yStart -= heightOff;
		boolean[] spawnBlock = new boolean[width * width * height];

		final int W = width - 1, H = height - 1;
		// width 16; height 8

		for (int i = 0, e = rand.nextInt(4) + 4; i < e; ++i) {
			double xSize = rand.nextDouble() * 6.0D + 3.0D;
			// 6 + 3 = 9; 6 * 2 = 12; + 3 = 15; 9 * 2 = 18
			double ySize = rand.nextDouble() * 4.0D + 2.0D;
			// 4 + 2 = 6; 4 * 2 = 8; + 2 = 10; 6 * 2 = 12
			double zSize = rand.nextDouble() * 6.0D + 3.0D;
			// (WIDTH + WIDTH_OFFSET * 2) / 3 + (WIDTH + WIDTH_OFFSET * 2) / 6?
			double xCenter = rand.nextDouble() * (width - xSize - 2.0D) + 1.0D + xSize / 2.0D;
			// (16 - 9 - 2) + 1 + 4.5 = 10.5; (16 - 3 - 2) + 1 + 1.5 = 13.5;
			// ( 0 - 0 - 0) + 1 + 4.5 =  5.5; ( 0 - 0 - 0) + 1 + 1.5 =  2.5;
			// ( 8 - 4.5 - 1) + 1 + 4.5 =  8; ( 8 - 1.5 - 1) + 1 + 1.5 =  8; equally distributed around center width
			double yCenter = rand.nextDouble() * (height - ySize - 4.0D) + 2.0D + ySize / 2.0D;
			// (8 - 6 - 4) + 2 + 3 = 3; (8 - 2 - 4) + 2 + 1 = 5;
			// (0 - 0 - 0) + 2 + 3 = 5; (0 - 0 - 0) + 2 + 1 = 3;
			// (4 - 3 - 2) + 2 + 3 = 4; (4 - 1 - 2) + 2 + 1 = 4; equally distributed around center height.
			double zCenter = rand.nextDouble() * (width - zSize - 2.0D) + 1.0D + zSize / 2.0D;
			// (WIDTH - zSize - WIDTH_OFFSET * 2) + WIDTH_OFFSET + zSize / 2; ?

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
							net.minecraft.block.material.Material material = getBlockState(world, xStart + x, yStart + y, zStart + z).getMaterial();
							if (material.isLiquid()) {
								return false;
							}
						} else {
							if (!canGenerateInBlock(world, xStart + x, yStart + y, zStart + z, material)) {
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
							generateBlock(world, rand, xStart + x, yStart + y, zStart + z, material, resource);
						} else if (canGenerateInBlock(world, xStart + x, yStart + y, zStart + z, material)) {
							generateBlock(world, rand, xStart + x, yStart + y, zStart + z, filler);
						}
					}
				}
			}
		}

		for (x = 0; x < width; ++x) {
			for (z = 0; z < width; ++z) {
				for (y = 0; y < height; ++y) {
					if (spawnBlock[(x * width + z) * height + y] &&
							getBlockState(world, xStart + x, yStart + y - 1, zStart + z).getBlock().equals(Blocks.DIRT) &&
							world.getLightFor(LightType.SKY, new BlockPos(xStart + x, yStart + y, zStart + z)) > 0) {
						Biome bgb = world.getBiome(new BlockPos(xStart + x, 0, zStart + z));
						setBlockState(world, new BlockPos(xStart + x, yStart + y - 1, zStart + z), bgb.getSurfaceBuilderConfig().getTop());
					}
				}
			}
		}

		if (outline != null && outline.size() > 0) {
			data.setValue("fill-height", heightOff).setValue("width", width).setValue("height", height);
			for (x = 0; x < width; ++x) {
				for (z = 0; z < width; ++z) {
					for (y = 0; y < height; ++y) {
						boolean flag = !spawnBlock[(x * width + z) * height + y] && ((x < W && spawnBlock[((x + 1) * width + z) * height + y]) || (x > 0 && spawnBlock[((x - 1) * width + z) * height + y]) || (z < W && spawnBlock[(x * width + (z + 1)) * height + y]) || (z > 0 && spawnBlock[(x * width + (z - 1)) * height + y]) || (y < H && spawnBlock[(x * width + z) * height + (y + 1)]) || (y > 0 && spawnBlock[(x * width + z) * height + (y - 1)]));

						if (flag) {
							data.setValue("layer", y).setPosition(new BlockPos(xStart + x, yStart + y, zStart + z));
							if (outlineCondition.checkCondition(world, rand, data))
								generateBlock(world, rand, xStart + x, yStart + y, zStart + z, outline);
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

}
