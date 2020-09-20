package cofh.cofhworld.world.generator;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

public class WorldGenConsecutive extends WorldGen {

	private final WorldGen[] generators;
	private int generatorIndex;

	public WorldGenConsecutive(List<WorldGen> values) {

		generators = values.toArray(new WorldGen[0]);
	}

	@Override
	public void setDecorationDefaults() {

		generatorIndex = 0;
		for (WorldGen gen : generators) {
			gen.setDecorationDefaults();
		}
	}

	@Override
	public boolean generate(IWorld world, Random rand, BlockPos pos) {

		generatorIndex = (generatorIndex + 1) % generators.length;

		return generators[generatorIndex].generate(world, rand, pos);
	}

}
