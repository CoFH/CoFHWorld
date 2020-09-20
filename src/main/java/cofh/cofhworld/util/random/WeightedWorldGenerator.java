package cofh.cofhworld.util.random;

import cofh.cofhworld.world.generator.WorldGen;
import net.minecraft.util.WeightedRandom;

public final class WeightedWorldGenerator extends WeightedRandom.Item {

	public final WorldGen generator;

	public WeightedWorldGenerator(WorldGen worldgen) {

		this(worldgen, 100);
	}

	public WeightedWorldGenerator(WorldGen worldgen, int weight) {

		super(weight);
		generator = worldgen;
	}

}
