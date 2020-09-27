package cofh.cofhworld.parser.generator.builders;

import cofh.cofhworld.data.condition.ConstantCondition;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.operation.BoundedProvider;
import cofh.cofhworld.parser.generator.builders.base.BuilderShape;
import cofh.cofhworld.world.generator.WorldGenPlate;

public class BuilderPlate extends BuilderShape<WorldGenPlate> {

	private static final INumberProvider ONE = new ConstantProvider(1);

	private INumberProvider radius;
	private INumberProvider height = ONE;

	private ICondition slim = ConstantCondition.FALSE;

	public void setRadius(INumberProvider radius) {

		this.radius = new BoundedProvider(radius, 0, 32);
	}

	public void setHeight(INumberProvider height) {

		this.height = new BoundedProvider(height, 0, 256);
	}

	public void setSlim(ICondition slim) {

		this.slim = slim;
	}

	@Override
	public WorldGenPlate build() {

		return new WorldGenPlate(resource, material, shape, radius, height, slim);
	}
}
