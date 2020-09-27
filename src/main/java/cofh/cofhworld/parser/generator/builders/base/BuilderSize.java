package cofh.cofhworld.parser.generator.builders.base;

import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.world.generator.WorldGen;

public abstract class BuilderSize<T extends WorldGen> extends BaseBuilder<T> {

	protected INumberProvider size;

	public void setSize(INumberProvider value) {

		size = value;
	}
}
