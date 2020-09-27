package cofh.cofhworld.parser.generator.builders;

import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.operation.BoundedProvider;
import cofh.cofhworld.parser.generator.builders.base.BuilderSize;
import cofh.cofhworld.world.generator.WorldGenCluster;

public class BuilderCluster extends BuilderSize<WorldGenCluster> {

	private Type type = Type.TINY;

	public void setType(Type type) {

		this.type = type;
	}

	public void setSize(INumberProvider size) {

		super.setSize(new BoundedProvider(size, 0, 64));
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
