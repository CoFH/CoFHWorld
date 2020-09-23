package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.condition.operation.BinaryCondition;
import cofh.cofhworld.data.condition.operation.ComparisonCondition;
import cofh.cofhworld.data.condition.world.WorldValueCondition;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.data.DataProvider;
import cofh.cofhworld.data.numbers.random.UniformRandomProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.util.random.WeightedNBTTag;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import static java.lang.Math.abs;

public class WorldGenDungeon extends WorldGen {

	final private static INumberProvider TWO = new ConstantProvider(2), THREE = new ConstantProvider(3);
	final private static INumberProvider TWO_OR_THREE = new UniformRandomProvider(TWO, THREE);
	final private static ICondition IS_AIR = new WorldValueCondition("IS_AIR");
	final private static ICondition ONE_TO_FIVE = new BinaryCondition(
			new ComparisonCondition(new DataProvider("holes"), new ConstantProvider(1), "GREATER_THAN_OR_EQUAL"),
			new ComparisonCondition(new DataProvider("holes"), new ConstantProvider(5), "LESS_THAN_OR_EQUAL"),
			"AND");

	private final Material[] material;
	private final List<WeightedBlock> spawners;
	private final List<WeightedBlock> walls;
	public List<WeightedBlock> chests;
	public List<WeightedBlock> floor;
	public List<WeightedBlock> filler;
	public INumberProvider radiusX = TWO_OR_THREE;
	public INumberProvider radiusZ = TWO_OR_THREE;
	public INumberProvider height = THREE;
	public ICondition validHoleCount = ONE_TO_FIVE, holeCondition = IS_AIR;
	public INumberProvider chestCount = TWO, chestAttempts = THREE;

	public WorldGenDungeon(List<WeightedBlock> blocks, List<Material> materials, List<WeightedBlock> spawners) {

		material = materials.toArray(new Material[0]);
		walls = blocks;
		floor = walls;
		this.spawners = spawners;
		try {
			chests = Collections.singletonList(new WeightedBlock(100, Blocks.CHEST.getDefaultState(),
					Collections.singletonList(new WeightedNBTTag(JsonToNBT.getTagFromJson("{LootTable:\"minecraft:chests/simple_dungeon\"}")))));
		} catch (CommandSyntaxException e) {
			throw new AssertionError("Oops.", e);
		}
		filler = Collections.singletonList(new WeightedBlock(Blocks.AIR));
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

		BlockState state = world.getBlockState(new BlockPos(x, y, z));
		for (int j = 0, e = blocks.size(); j < e; ++j) {
			WeightedBlock genBlock = blocks.get(j);
			if (state.equals(genBlock.getState()))
				return true;
		}
		return false;
	}

}
