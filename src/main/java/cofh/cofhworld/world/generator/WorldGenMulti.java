package cofh.cofhworld.world.generator;

import cofh.cofhworld.util.random.WeightedWorldGenerator;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldGenMulti extends WorldGenerator {

	private final List<WeightedWorldGenerator> generators;

	public WorldGenMulti(ArrayList<WeightedWorldGenerator> values) {

		generators = values;
	}

	@Override
	public boolean generate(World world, Random random, BlockPos pos) {

		WeightedWorldGenerator gen = WeightedRandom.getRandomItem(random, generators);
		return gen.generator.generate(world, random, pos);
	}

}
