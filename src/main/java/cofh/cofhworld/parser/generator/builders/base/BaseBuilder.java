package cofh.cofhworld.parser.generator.builders.base;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.IBuilder;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;

import java.util.List;

public abstract class BaseBuilder<T extends WorldGen> implements IBuilder<T> {

	protected final List<WeightedBlock> resource;
	protected final List<Material> material;

	public BaseBuilder(List<WeightedBlock> resource, List<Material> material) {

		this.resource = resource;
		this.material = material;
	}
}