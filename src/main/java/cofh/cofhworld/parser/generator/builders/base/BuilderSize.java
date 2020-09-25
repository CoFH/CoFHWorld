package cofh.cofhworld.parser.generator.builders.base;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.util.random.WeightedBlock;

import java.util.List;

public abstract class BuilderSize<T> extends BaseBuilder<T> {

	protected INumberProvider size;

	public BuilderSize(List<WeightedBlock> resource, List<Material> material) {

		super(resource, material);
	}

	public void setSize(INumberProvider value) {

		size = value;
	}
}
