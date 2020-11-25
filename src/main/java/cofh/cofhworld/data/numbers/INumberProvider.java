package cofh.cofhworld.data.numbers;

import cofh.cofhworld.data.DataHolder;
import net.minecraft.world.IWorld;

import java.util.Random;

public interface INumberProvider {

	default int intValue(IWorld world, Random rand, DataHolder data) {

		return (int) longValue(world, rand, data);
	}

	long longValue(IWorld world, Random rand, DataHolder data);

	default float floatValue(IWorld world, Random rand, DataHolder data) {

		return (float) doubleValue(world, rand, data);
	}

	default double doubleValue(IWorld world, Random rand, DataHolder data) {

		return longValue(world, rand, data);
	}

}
