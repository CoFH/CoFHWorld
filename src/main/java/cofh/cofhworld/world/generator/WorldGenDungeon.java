package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

import static java.lang.Math.abs;

public class WorldGenDungeon extends WorldGen {

	private final List<WeightedBlock> walls;
	private final Material[] material;
	private final List<WeightedBlock> spawners;
	private final List<WeightedBlock> floor;
	private final List<WeightedBlock> chests;
	private final List<WeightedBlock> filler;
	private final INumberProvider radiusX;
	private final INumberProvider radiusZ;
	private final INumberProvider height;
	private final ICondition validHoleCount, holeCondition;
	private final INumberProvider chestCount, chestAttempts;

	public WorldGenDungeon(List<WeightedBlock> blocks, List<Material> materials, List<WeightedBlock> spawners, List<WeightedBlock> floor,
			List<WeightedBlock> chests, List<WeightedBlock> filler, INumberProvider radiusX, INumberProvider radiusZ, INumberProvider height,
			ICondition validHoleCount, ICondition holeCondition, INumberProvider chestCount, INumberProvider chestAttempts) {

		walls = blocks;
		material = materials.toArray(new Material[0]);
		this.spawners = spawners;
		this.floor = floor;
		this.chests = chests;
		this.filler = filler;
		this.radiusX = radiusX;
		this.radiusZ = radiusZ;
		this.height = height;
		this.validHoleCount = validHoleCount;
		this.holeCondition = holeCondition;
		this.chestCount = chestCount;
		this.chestAttempts = chestAttempts;
	}

	@Override
	public boolean generate(IWorld world, Random rand, final DataHolder data) {

		final BlockPos start = data.getPosition();

		int xStart = start.getX();
		int yStart = start.getY();
		int zStart = start.getZ();
		if (yStart <= 2) {
			return false;
		}

		final int height = this.height.intValue(world, rand, data);
		final int xWidth = this.radiusX.intValue(world, rand, data.setValue("height", height));
		final int zWidth = this.radiusZ.intValue(world, rand, data.setValue("radius-x", xWidth));
		final int floor = yStart - 1, ceiling = yStart + height + 1;
		data.setValue("radius-z", zWidth);

		int holes = 0;
		int x, y, z;

		for (x = xStart - xWidth - 1; x <= xStart + xWidth + 1; ++x) {
			for (z = zStart - zWidth - 1; z <= zStart + zWidth + 1; ++z) {
				for (y = floor; y <= ceiling; ++y) {

					if (y == floor && !canGenerateInBlock(world, x, y, z, material)) {
						return false;
					}

					if (y == ceiling && !canGenerateInBlock(world, x, y, z, material)) {
						return false;
					}

					if (y == yStart && (abs(x - xStart) == xWidth + 1 || abs(z - zStart) == zWidth + 1) &&
							holeCondition.checkCondition(world, rand, data.setPosition(new BlockPos(x, y, z))) &&
							holeCondition.checkCondition(world, rand, data.setPosition(new BlockPos(x, y + 1, z)))) {
						++holes;
					}
				}
			}
		}

		if (!validHoleCount.checkCondition(world, rand, data.setValue("holes", holes))) {
			return false;
		}

		for (x = xStart - xWidth - 1; x <= xStart + xWidth + 1; ++x) {
			for (z = zStart - zWidth - 1; z <= zStart + zWidth + 1; ++z) {
				for (y = yStart + height; y >= floor; --y) {

					l: if (y != floor) {
						if ((abs(x - xStart) != xWidth + 1 && abs(z - zStart) != zWidth + 1)) {
							generateBlock(world, rand, x, y, z, filler);
						} else if (!canGenerateInBlock(world, x, y - 1, z, material)) {
							generateBlock(world, rand, x, y, z, filler);
						} else {
							break l;
						}
						continue;
					}
					if (y == floor) { // material validated during hole checks
						generateBlock(world, rand, x, y, z, this.floor);
					} else if (y == ceiling || // material validated during hole checks
							canGenerateInBlock(world, x, y, z, material)) {
						generateBlock(world, rand, x, y, z, walls);
					}
				}
			}
		}

		WeightedBlock chest = selectBlock(rand, chests);

		for (int i = chestCount.intValue(world, rand, data.setPosition(start).setBlock(chest)); i-- > 0;) {
			for (int j = chestAttempts.intValue(world, rand, data.setValue("current-chest", i + 1)); j-- > 0;) {
				x = xStart + nextInt(rand, xWidth * 2 + 1) - xWidth;
				z = zStart + nextInt(rand, zWidth * 2 + 1) - zWidth;

				if (isBlock(world, x, yStart, z, this.filler)) {
					int walls = 0;

					if (isBlock(world, x - 1, yStart, z, this.walls)) {
						++walls;
					}

					if (isBlock(world, x + 1, yStart, z, this.walls)) {
						++walls;
					}

					if (isBlock(world, x, yStart, z - 1, this.walls)) {
						++walls;
					}

					if (isBlock(world, x, yStart, z + 1, this.walls)) {
						++walls;
					}

					if (walls >= 1 && walls <= 2) {
						setBlock(world, rand, new BlockPos(x, yStart,  z), chest);

						break;
					}
				}
			}
		}

		generateBlock(world, rand, xStart, yStart, zStart, spawners);

		return true;
	}

	private static int nextInt(Random rand, int v) {

		if (v <= 1) {
			return 0;
		}
		return rand.nextInt(v);
	}

	private boolean isBlock(IWorld world, int x, int y, int z, List<WeightedBlock> blocks) {

		BlockState state = getBlockState(world, x, y, z);
		for (int j = 0, e = blocks.size(); j < e; ++j) {
			WeightedBlock genBlock = blocks.get(j);
			if (state.equals(genBlock.getState()))
				return true;
		}
		return false;
	}

}
