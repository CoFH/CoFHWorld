package cofh.cofhworld.data.numbers.operation;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import net.minecraft.world.IWorld;

import java.util.Objects;
import java.util.Random;

public class BoundedProvider implements INumberProvider {

	protected final INumberProvider value;
	protected final INumberProvider min, max;

	public BoundedProvider(INumberProvider value, Number min, Number max) {

		this(Objects.requireNonNull(value), new ConstantProvider(min), new ConstantProvider(max));
	}

	public BoundedProvider(INumberProvider value, INumberProvider min, INumberProvider max) {

		this.value = value;
		this.min = min;
		this.max = max;
	}

	@Override
	public long longValue(IWorld world, Random rand, DataHolder data) {

		return Math.min(Math.max(value.longValue(world, rand, data), min.longValue(world, rand, data)), max.longValue(world, rand, data));
	}

	@Override
	public double doubleValue(IWorld world, Random rand, DataHolder data) {

		return Math.min(Math.max(value.doubleValue(world, rand, data), min.doubleValue(world, rand, data)), max.doubleValue(world, rand, data));
	}

}
