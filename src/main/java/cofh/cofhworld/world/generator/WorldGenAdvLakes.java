package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.block.MaterialPropertyMaterial;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.condition.operation.BinaryCondition;
import cofh.cofhworld.data.condition.operation.ComparisonCondition;
import cofh.cofhworld.data.condition.random.RandomCondition;
import cofh.cofhworld.data.condition.world.MaterialCondition;
import cofh.cofhworld.data.condition.world.WorldValueCondition;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.data.DataProvider;
import cofh.cofhworld.data.numbers.world.DirectionalScanner;
import cofh.cofhworld.data.numbers.world.WorldValueProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WorldGenAdvLakes extends WorldGen {

	private static final List<WeightedBlock> GAP_BLOCK = Collections.singletonList(WeightedBlock.AIR);

	private final cofh.cofhworld.data.block.Material[] material;
	private final List<WeightedBlock> resource;

	private List<WeightedBlock> gapBlock = GAP_BLOCK;

	private List<WeightedBlock> outlineBlock = null;
	private ICondition outlineCondition = new BinaryCondition(
			new BinaryCondition(
					new ComparisonCondition(
							new DataProvider("layer"),
							new DataProvider("fill-height"),
							"LESS_THAN"),
					new RandomCondition(),
					"AND"),
			new MaterialCondition(Collections.singletonList(new MaterialPropertyMaterial(true, "SOLID"))),
			"AND");

	private INumberProvider width;
	private INumberProvider height;

	public WorldGenAdvLakes(List<WeightedBlock> resource, List<cofh.cofhworld.data.block.Material> materials) {

		this.resource = resource;
		if (materials == null) {
			material = null;
		} else {
			material = materials.toArray(new cofh.cofhworld.data.block.Material[0]);
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
							generateBlock(world, rand, xStart + x, yStart + y, zStart + z, gapBlock);
						}
					}
				}
			}
		}

		for (x = 0; x < width; ++x) {
			for (z = 0; z < width; ++z) {
				for (y = 0; y < height; ++y) {
					if (spawnBlock[(x * width + z) * height + y] &&
							world.getBlockState(new BlockPos(xStart + x, yStart + y - 1, zStart + z)).getBlock().equals(Blocks.DIRT) &&
							world.getLightFor(LightType.SKY, new BlockPos(xStart + x, yStart + y, zStart + z)) > 0) {
						Biome bgb = world.getBiome(new BlockPos(xStart + x, 0, zStart + z));
						setBlockState(world, new BlockPos(xStart + x, yStart + y - 1, zStart + z), bgb.getSurfaceBuilderConfig().getTop());
					}
				}
			}
		}

		if (outlineBlock != null && outlineBlock.size() > 0) {
			data.setValue("fill-height", heightOff).setValue("width", width).setValue("height", height);
			for (x = 0; x < width; ++x) {
				for (z = 0; z < width; ++z) {
					for (y = 0; y < height; ++y) {
						boolean flag = !spawnBlock[(x * width + z) * height + y] && ((x < W && spawnBlock[((x + 1) * width + z) * height + y]) || (x > 0 && spawnBlock[((x - 1) * width + z) * height + y]) || (z < W && spawnBlock[(x * width + (z + 1)) * height + y]) || (z > 0 && spawnBlock[(x * width + (z - 1)) * height + y]) || (y < H && spawnBlock[(x * width + z) * height + (y + 1)]) || (y > 0 && spawnBlock[(x * width + z) * height + (y - 1)]));

						if (flag) {
							data.setValue("layer", y).setPosition(new BlockPos(xStart + x, yStart + y, zStart + z));
							if (outlineCondition.checkCondition(world, rand, data))
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

	public WorldGenAdvLakes setOutlineCondition(ICondition outline) {

		this.outlineCondition = outline;
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
