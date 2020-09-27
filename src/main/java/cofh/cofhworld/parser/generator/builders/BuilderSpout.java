package cofh.cofhworld.parser.generator.builders;

import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.operation.BoundedProvider;
import cofh.cofhworld.parser.generator.builders.base.BuilderShape;
import cofh.cofhworld.world.generator.WorldGenSpout;

public class BuilderSpout extends BuilderShape<WorldGenSpout> {

	private INumberProvider radius;
	private INumberProvider height;

	public void setRadius(INumberProvider radius) {

		this.radius = new BoundedProvider(radius, 0, 32);
	}

	public void setHeight(INumberProvider height) {

		this.height = new BoundedProvider(height, 0, 256);
	}

	@Override
	public WorldGenSpout build() {

		return new WorldGenSpout(resource, material, radius, height, shape);
	}
}

