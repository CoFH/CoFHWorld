package cofh.cofhworld.world.generator;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

public class WorldGenSequential extends WorldGen {

	private final WorldGen[] generators;

	public WorldGenSequential(List<WorldGen> values) {

		generators = values.toArray(new WorldGen[0]);
	}

	@Override
	public void setDecorationDefaults() {

		for (WorldGen gen : generators) {
			gen.setDecorationDefaults();
		}
	}

	@Override
	public boolean generate(IWorld world, Random random, BlockPos pos) {

		WorldGen[] generators = this.generators;
		boolean r = false;

		for (int i = 0, e = generators.length; i < e; ++i) {
			WorldGen gen = generators[i];
			r |= gen.generate(world, random, pos);
		}

		return r;
	}

}
