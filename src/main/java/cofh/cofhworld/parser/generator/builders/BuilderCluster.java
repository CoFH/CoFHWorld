package cofh.cofhworld.parser.generator.builders;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.parser.generator.builders.base.BaseBuilder;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGenCluster;

import java.util.List;

public class BuilderCluster extends BaseBuilder<WorldGenCluster> {

	private INumberProvider clusterSize = null;
	private Type type = Type.TINY;

	public BuilderCluster(List<WeightedBlock> resource, List<Material> material) {

		super(resource, material);
	}

	public BuilderCluster setClusterSize(INumberProvider value) {

		clusterSize = value;
		return this;
	}

	public BuilderCluster setType(Type type) {

		this.type = type;
		return this;
	}

	@Override
	public WorldGenCluster build() {

		switch (type) {
			default:
			case TINY:
				return new WorldGenCluster.Tiny(resource, clusterSize, material);
			case SPARSE:
				return new WorldGenCluster.Sparse(resource, clusterSize, material);
			case VANILLA:
				return new WorldGenCluster(resource, clusterSize, material);
		}
	}

	public static enum Type {
		TINY,
		SPARSE,
		VANILLA;
	}
}
