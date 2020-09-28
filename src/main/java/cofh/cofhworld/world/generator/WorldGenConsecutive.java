package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

public class WorldGenConsecutive extends WorldGen {

	private final WorldGen[] generators;
	private final ThreadLocal<GeneratorIndex> generatorIndex = ThreadLocal.withInitial(GeneratorIndex::new);

	public WorldGenConsecutive(List<WorldGen> values) {

		generators = values.toArray(new WorldGen[0]);
	}

	@Override
	public void setDecorationDefaults() {

		generatorIndex.get().reset();
		for (WorldGen gen : generators) {
			gen.setDecorationDefaults();
		}
	}

	@Override
	public boolean generate(IWorld world, Random rand, DataHolder data) {

		return generators[generatorIndex.get().incrementAndGet(generators.length)].generate(world, rand, data.getPosition());
	}

	private final static class GeneratorIndex {
		private int index = -1;
		public int incrementAndGet(int modulo) {

			return index = (index + 1) % modulo;
		}
		public void reset() {
			index = -1;
		}
	}

}
