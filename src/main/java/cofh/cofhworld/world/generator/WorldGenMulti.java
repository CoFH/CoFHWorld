package cofh.cofhworld.world.generator;

import cofh.cofhworld.util.random.WeightedWorldGenerator;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldGenMulti extends WorldGen {

	private final List<WeightedWorldGenerator> generators;

	public WorldGenMulti(ArrayList<WeightedWorldGenerator> values) {

		generators = values;
	}

	@Override
	public void setDecorationDefaults() {

		for (WeightedWorldGenerator gen : generators) {
			gen.generator.setDecorationDefaults();
		}
	}

	@Override
	public boolean generate(World world, Random random, BlockPos pos) {

		WeightedWorldGenerator gen = WeightedRandom.getRandomItem(random, generators);
		return gen.generator.generate(world, random, pos);
	}

}
