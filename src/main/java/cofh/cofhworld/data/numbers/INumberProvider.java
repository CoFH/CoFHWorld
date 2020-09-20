package cofh.cofhworld.data.numbers;

import cofh.cofhworld.data.DataHolder;
import net.minecraft.world.IWorldReader;

import java.util.Random;

public interface INumberProvider {

	default int intValue(IWorldReader world, Random rand, DataHolder data) {

		return (int) longValue(world, rand, data);
	}

	long longValue(IWorldReader world, Random rand, DataHolder data);

	default float floatValue(IWorldReader world, Random rand, DataHolder data) {

		return (float) doubleValue(world, rand, data);
	}

	default double doubleValue(IWorldReader world, Random rand, DataHolder data) {

		return longValue(world, rand, data);
	}

}
