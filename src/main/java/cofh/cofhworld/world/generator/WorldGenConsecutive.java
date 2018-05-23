package cofh.cofhworld.world.generator;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.List;
import java.util.Random;

public class WorldGenConsecutive extends WorldGenerator {

	private final WorldGenerator[] generators;
	private int generatorIndex;

	public WorldGenConsecutive(List<WorldGenerator> values) {

		generators = values.toArray(new WorldGenerator[values.size()]);
	}

	@Override
	public void setDecorationDefaults() {

		generatorIndex = 0;
		for (WorldGenerator gen : generators) {
			gen.setDecorationDefaults();
		}
	}

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {

		generatorIndex = (generatorIndex + 1) % generators.length;

		return generators[generatorIndex].generate(world, rand, pos);
	}

}
