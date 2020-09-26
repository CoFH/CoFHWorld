package cofh.cofhworld.parser.generator.builders.base;

import cofh.cofhworld.parser.IBuilder;
import cofh.cofhworld.world.generator.WorldGen;

import java.util.List;

public abstract class BuilderGenerator implements IBuilder<WorldGen> {

	protected List<WorldGen> generators;

	public void setGenerators(List<WorldGen> generators) {

		this.generators = generators;
	}
}
