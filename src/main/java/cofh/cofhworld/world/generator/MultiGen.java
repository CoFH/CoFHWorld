package cofh.cofhworld.world.generator;

import cofh.cofhworld.feature.Feature;
import cofh.cofhworld.feature.IGenerator;
import cofh.cofhworld.util.WeightedRandomWorldGenerator;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MultiGen implements IGenerator {

	private final List<WeightedRandomWorldGenerator> generators;

	public MultiGen(ArrayList<WeightedRandomWorldGenerator> values) {

		generators = values;
	}

	@Override
	public boolean generate(Feature feature, World world, Random random, BlockPos pos) {

		WeightedRandomWorldGenerator gen = WeightedRandom.getRandomItem(random, generators);
		return gen.generator.generate(feature, world, random, pos);
	}

}
