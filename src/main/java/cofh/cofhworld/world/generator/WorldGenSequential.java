package cofh.cofhworld.world.generator;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.List;
import java.util.Random;

public class WorldGenSequential extends WorldGenerator {

	private final WorldGenerator[] generators;

	public WorldGenSequential(List<WorldGenerator> values) {

		generators = values.toArray(new WorldGenerator[values.size()]);
	}

	@Override
	public boolean generate(World world, Random random, BlockPos pos) {

		WorldGenerator[] generators = this.generators;
		boolean r = false;

		for (int i = 0, e = generators.length; i < e; ++i) {
			WorldGenerator gen = generators[i];
			r |= gen.generate(world, random, pos);
		}

		return r;
	}

}
