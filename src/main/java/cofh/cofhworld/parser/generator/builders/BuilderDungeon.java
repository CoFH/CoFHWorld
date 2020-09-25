package cofh.cofhworld.parser.generator.builders;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.condition.operation.BinaryCondition;
import cofh.cofhworld.data.condition.operation.ComparisonCondition;
import cofh.cofhworld.data.condition.world.WorldValueCondition;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.data.DataProvider;
import cofh.cofhworld.data.numbers.operation.UnaryMathProvider;
import cofh.cofhworld.data.numbers.random.UniformRandomProvider;
import cofh.cofhworld.parser.generator.builders.base.BaseBuilder;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.util.random.WeightedNBTTag;
import cofh.cofhworld.world.generator.WorldGenDungeon;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.JsonToNBT;

import java.util.Collections;
import java.util.List;

public class BuilderDungeon extends BaseBuilder<WorldGenDungeon> {

	final private static INumberProvider TWO = new ConstantProvider(2), THREE = new ConstantProvider(3);
	final private static INumberProvider TWO_OR_THREE = new UniformRandomProvider(TWO, new UnaryMathProvider(THREE, "INCREMENT"));
	final private static ICondition IS_AIR = new WorldValueCondition("IS_AIR");
	final private static ICondition ONE_TO_FIVE = new BinaryCondition(
			new ComparisonCondition(new DataProvider("holes"), new ConstantProvider(1), "GREATER_THAN_OR_EQUAL"),
			new ComparisonCondition(new DataProvider("holes"), new ConstantProvider(5), "LESS_THAN_OR_EQUAL"),
			"AND");
	final private static List<WeightedBlock> DUNGEON_CHEST, AIR_FILLER = Collections.singletonList(WeightedBlock.AIR_CAVE);
	static {
		try {
			DUNGEON_CHEST = Collections.singletonList(new WeightedBlock(100, Blocks.CHEST.getDefaultState(),
					Collections.singletonList(new WeightedNBTTag(JsonToNBT.getTagFromJson("{LootTable:\"minecraft:chests/simple_dungeon\"}")))));
		} catch (CommandSyntaxException e) {
			throw new AssertionError("Oops.", e);
		}
	}

	private List<WeightedBlock> spawners = AIR_FILLER;
	private List<WeightedBlock> floor;
	private List<WeightedBlock> filler = AIR_FILLER;
	private List<WeightedBlock> chests = DUNGEON_CHEST;

	private INumberProvider height = THREE;
	private INumberProvider radiusX = TWO_OR_THREE;
	private INumberProvider radiusZ = TWO_OR_THREE;
	private INumberProvider chestCount = TWO;
	private INumberProvider chestAttempts = THREE;

	private ICondition holeCount = ONE_TO_FIVE;
	private ICondition holeCond = IS_AIR;

	public BuilderDungeon(List<WeightedBlock> resource, List<Material> material) {

		super(resource, material);
		floor = resource;
	}

	public void setSpawners(List<WeightedBlock> spawners) {

		this.spawners = spawners;
	}

	public void setFloor(List<WeightedBlock> floor) {

		this.floor = floor;
	}

	public void setFiller(List<WeightedBlock> filler) {

		this.filler = filler;
	}

	public void setChests(List<WeightedBlock> chests) {

		this.chests = chests;
	}

	public void setHeight(INumberProvider height) {

		this.height = height;
	}

	public void setRadiusX(INumberProvider radiusX) {

		this.radiusX = radiusX;
	}

	public void setRadiusZ(INumberProvider radiusZ) {

		this.radiusZ = radiusZ;
	}

	public void setChestCount(INumberProvider chestCount) {

		this.chestCount = chestCount;
	}

	public void setChestAttempts(INumberProvider chestAttempts) {

		this.chestAttempts = chestAttempts;
	}

	public void setHoleCount(ICondition holeCount) {

		this.holeCount = holeCount;
	}

	public void setHoleCondition(ICondition holeCondition) {

		this.holeCond = holeCondition;
	}

	@Override
	public WorldGenDungeon build() {

		return new WorldGenDungeon(resource, material, spawners, floor, chests, filler, radiusX, radiusZ, height, holeCount, holeCond, chestCount, chestAttempts);
	}
}
