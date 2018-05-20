package cofh.cofhworld.util.random;

import net.minecraft.util.WeightedRandom;
import net.minecraft.world.gen.feature.WorldGenerator;

public final class WeightedWorldGenerator extends WeightedRandom.Item {

	public final WorldGenerator generator;

	public WeightedWorldGenerator(WorldGenerator worldgen) {

		this(worldgen, 100);
	}

	public WeightedWorldGenerator(WorldGenerator worldgen, int weight) {

		super(weight);
		generator = worldgen;
	}

}
