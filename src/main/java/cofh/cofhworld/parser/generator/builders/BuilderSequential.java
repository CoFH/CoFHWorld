package cofh.cofhworld.parser.generator.builders;

import cofh.cofhworld.parser.generator.builders.base.BuilderGenerator;
import cofh.cofhworld.world.generator.WorldGenSequential;

import javax.annotation.Nonnull;

public class BuilderSequential extends BuilderGenerator<WorldGenSequential> {

	@Nonnull
	@Override
	public WorldGenSequential build() {

		return new WorldGenSequential(generators);
	}
}
