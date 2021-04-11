package cofh.cofhworld.parser.generator.builders.base;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.IBuilder;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;

import java.util.List;

public abstract class BaseBuilder<T extends WorldGen> implements IBuilder<T> {

	protected List<WeightedBlock> resource;
	protected List<Material> material;

	final public void setResource(List<WeightedBlock> resource) {

		this.resource = resource;
	}

	final public void setMaterial(List<Material> material) {

		this.material = material;
	}
}
