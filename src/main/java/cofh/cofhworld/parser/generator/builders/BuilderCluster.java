package cofh.cofhworld.parser.generator.builders;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.parser.generator.builders.base.BuilderSize;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGenCluster;

import java.util.List;

public class BuilderCluster extends BuilderSize<WorldGenCluster> {

	private Type type = Type.TINY;

	public BuilderCluster(List<WeightedBlock> resource, List<Material> material) {

		super(resource, material);
	}

	public void setType(Type type) {

		this.type = type;
	}

	@Override
	public WorldGenCluster build() {

		switch (type) {
			default:
			case TINY:
				return new WorldGenCluster.Tiny(resource, size, material);
			case SPARSE:
				return new WorldGenCluster.Sparse(resource, size, material);
			case VANILLA:
				return new WorldGenCluster(resource, size, material);
		}
	}

	public static enum Type {
		TINY,
		SPARSE,
		VANILLA;
	}
}
