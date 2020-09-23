package cofh.cofhworld.data.numbers;

import cofh.cofhworld.data.DataHolder;
import net.minecraft.world.IWorldReader;

import java.util.Random;

public class ConstantProvider implements INumberProvider {

	final public static ConstantProvider ZERO = new ConstantProvider(0);

	protected Number min;

	public ConstantProvider(Number value) {

		if (value == null) {
			throw new IllegalArgumentException("Null value not allowed");
		}
		this.min = value;
	}

	public long longValue(IWorldReader world, Random rand, DataHolder data) {

		return min.longValue();
	}

	public double doubleValue(IWorldReader world, Random rand, DataHolder data) {

		return min.doubleValue();
	}

}
