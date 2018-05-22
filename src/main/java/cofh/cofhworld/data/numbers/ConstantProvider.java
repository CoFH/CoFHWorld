package cofh.cofhworld.data.numbers;

import net.minecraft.world.World;

import java.util.Random;

public class ConstantProvider implements INumberProvider {

	protected Number min;

	public ConstantProvider(Number value) {

		if (value == null) {
			throw new IllegalArgumentException("Null value not allowed");
		}
		this.min = value;
	}

	public long longValue(World world, Random rand, DataHolder data) {

		return min.longValue();
	}

	public double doubleValue(World world, Random rand, DataHolder data) {

		return min.doubleValue();
	}

}
