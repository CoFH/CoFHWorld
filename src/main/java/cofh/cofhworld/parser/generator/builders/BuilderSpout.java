package cofh.cofhworld.parser.generator.builders;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.generator.builders.base.BuilderShape;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGenSpout;

import java.util.List;

public class BuilderSpout extends BuilderShape<WorldGenSpout> {

	private INumberProvider radius;
	private INumberProvider height;

	public BuilderSpout(List<WeightedBlock> resource, List<Material> material) {

		super(resource, material);
	}

	public void setRadius(INumberProvider radius) {

		this.radius = radius;
	}

	public void setHeight(INumberProvider height) {

		this.height = height;
	}

	@Override
	public WorldGenSpout build() {

		return new WorldGenSpout(resource, material, radius, height, shape);
	}
}

