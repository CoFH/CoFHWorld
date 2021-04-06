package cofh.cofhworld.parser.generator.builders;

import cofh.cofhworld.parser.generator.builders.base.BuilderGenerator;
import cofh.cofhworld.world.generator.WorldGen;
import cofh.cofhworld.world.generator.WorldGenCyclic;

import javax.annotation.Nonnull;

public class BuilderCyclic extends BuilderGenerator {

	@Nonnull
	@Override
	public WorldGen build() {

		return new WorldGenCyclic(generators);
	}
}
