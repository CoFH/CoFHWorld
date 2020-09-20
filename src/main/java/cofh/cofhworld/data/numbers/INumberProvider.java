package cofh.cofhworld.data.numbers;

import cofh.cofhworld.data.DataHolder;
import net.minecraft.world.World;

import java.util.Random;

public interface INumberProvider {

	default int intValue(World world, Random rand, DataHolder data) {

		return (int) longValue(world, rand, data);
	}

	long longValue(World world, Random rand, DataHolder data);

	default float floatValue(World world, Random rand, DataHolder data) {

		return (float) doubleValue(world, rand, data);
	}

	default double doubleValue(World world, Random rand, DataHolder data) {

		return longValue(world, rand, data);
	}

}
