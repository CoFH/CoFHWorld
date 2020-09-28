package cofh.cofhworld.parser.generator.builders;

import cofh.cofhworld.data.condition.ConstantCondition;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.operation.BoundedProvider;
import cofh.cofhworld.parser.generator.builders.base.BuilderShape;
import cofh.cofhworld.world.generator.WorldGenSpout;

public class BuilderSpout extends BuilderShape<WorldGenSpout> {

	private INumberProvider radius;
	private INumberProvider height;

	private ICondition mirror = ConstantCondition.FALSE;

	public void setRadius(INumberProvider radius) {

		this.radius = new BoundedProvider(radius, 0, 32);
	}

	public void setHeight(INumberProvider height) {

		this.height = new BoundedProvider(height, 0, 256);
	}

	public void setMirror(ICondition mirror) {

		this.mirror = mirror;
	}

	@Override
	public WorldGenSpout build() {

		return new WorldGenSpout(resource, material, shape, radius, height, mirror);
	}
}

