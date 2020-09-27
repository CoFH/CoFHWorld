package cofh.cofhworld.parser.generator.builders;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ConstantCondition;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.condition.operation.BinaryCondition;
import cofh.cofhworld.data.condition.operation.ComparisonCondition;
import cofh.cofhworld.data.condition.random.RandomCondition;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.data.DataProvider;
import cofh.cofhworld.data.numbers.operation.UnaryMathProvider;
import cofh.cofhworld.data.numbers.random.UniformRandomProvider;
import cofh.cofhworld.parser.generator.builders.base.BuilderSize;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGenSmallTree;

import javax.annotation.Nonnull;
import java.util.List;

public class BuilderSmallTree extends BuilderSize<WorldGenSmallTree> {

	private static final INumberProvider HEIGHT =  new UniformRandomProvider(5, 5 + 3);
	private static final ICondition LEAF_VARIANCE = new BinaryCondition(
			new BinaryCondition(
			new ComparisonCondition(new UnaryMathProvider(new DataProvider("layer-x"),"abs"), new DataProvider("radius"),"NOT_EQUAL"),
			new ComparisonCondition(new UnaryMathProvider(new DataProvider("layer-z"),"abs"), new DataProvider("radius"),"NOT_EQUAL"),
			"OR"),
			new BinaryCondition(
					new RandomCondition(),
					new ComparisonCondition(new DataProvider("layer"), new DataProvider("height"), "NOT_EQUAL"),
			"AND"),
			"OR"
			);;

	private List<WeightedBlock> leaves;
	private Material[] surface;
	private INumberProvider height = HEIGHT;

	private ICondition treeChecks = ConstantCondition.TRUE;
	private ICondition waterLoving = ConstantCondition.FALSE; // TODO: more work on this logic
	private ICondition relaxedGrowth = ConstantCondition.FALSE;

	private ICondition leafVariance = LEAF_VARIANCE;

	public void setLeaves(List<WeightedBlock> leaves) {

		this.leaves = leaves;
	}

	public void setSurface(List<Material> surface) {

		if (surface.size() > 0) this.surface = surface.toArray(new Material[0]);
	}

	public void setHeight(INumberProvider height) {

		this.height = height;
	}

	public void setTreeChecks(ICondition treeChecks) {

		this.treeChecks = treeChecks;
	}

	public void setWaterLoving(ICondition waterLoving) {

		this.waterLoving = waterLoving;
	}

	public void setRelaxedGrowth(ICondition relaxedGrowth) {

		this.relaxedGrowth = relaxedGrowth;
	}

	public void setLeafVariance(ICondition leafVariance) {

		this.leafVariance = leafVariance;
	}

	@Nonnull
	@Override
	public WorldGenSmallTree build() {

		return new WorldGenSmallTree(resource, leaves, material, surface, height, treeChecks, waterLoving, relaxedGrowth, leafVariance);
	}
}
