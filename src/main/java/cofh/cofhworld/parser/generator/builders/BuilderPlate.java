package cofh.cofhworld.parser.generator.builders;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ConstantCondition;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.generator.builders.base.BuilderShape;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGenPlate;

import java.util.List;

public class BuilderPlate extends BuilderShape<WorldGenPlate> {

	private static final INumberProvider ONE = new ConstantProvider(1);

	private INumberProvider radius;
	private INumberProvider height = ONE;

	private ICondition slim = ConstantCondition.FALSE;

	public BuilderPlate(List<WeightedBlock> resource, List<Material> material) {

		super(resource, material);
	}

	public void setRadius(INumberProvider radius) {

		this.radius = radius;
	}

	public void setHeight(INumberProvider height) {

		this.height = height;
	}

	public void setSlim(ICondition slim) {

		this.slim = slim;
	}

	@Override
	public WorldGenPlate build() {

		return new WorldGenPlate(resource, material, shape, radius, height, slim);
	}
}
