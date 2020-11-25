package cofh.cofhworld.data.numbers;

import cofh.cofhworld.data.DataHolder;
import net.minecraft.world.IWorld;

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

	public long longValue(IWorld world, Random rand, DataHolder data) {

		return min.longValue();
	}

	public double doubleValue(IWorld world, Random rand, DataHolder data) {

		return min.doubleValue();
	}

}
