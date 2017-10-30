package cofh.cofhworld.util;

import cofh.cofhworld.feature.IGenerator;
import net.minecraft.util.WeightedRandom;

public final class WeightedRandomWorldGenerator extends WeightedRandom.Item {

	public final IGenerator generator;

	public WeightedRandomWorldGenerator(IGenerator worldgen) {

		this(worldgen, 100);
	}

	public WeightedRandomWorldGenerator(IGenerator worldgen, int weight) {

		super(weight);
		generator = worldgen;
	}

}
