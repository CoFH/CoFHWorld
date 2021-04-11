package cofh.cofhworld.parser.distribution.builders.base;

import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.world.IConfigurableFeatureGenerator;
import cofh.cofhworld.world.generator.WorldGen;

public abstract class BuilderGenerator<T extends IConfigurableFeatureGenerator> extends BaseBuilder<T> {

	protected WorldGen generator;
	protected INumberProvider clusterCount;

	public void setGenerator(WorldGen generator) {

		this.generator = generator;
	}

	public void setClusterCount(INumberProvider clusterCount) {

		this.clusterCount = clusterCount;
	}
}
