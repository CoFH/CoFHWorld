package cofh.cofhworld.data.numbers.random;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import net.minecraft.world.World;

import java.util.Random;

public class UniformRandomProvider extends SkellamRandomProvider {

	protected INumberProvider max;

	public UniformRandomProvider(Number min, Number max) {

		super(min);
		this.max = new ConstantProvider(max);
	}

	public UniformRandomProvider(INumberProvider min, INumberProvider max) {

		super(min);
		this.max = max;
	}

	@Override
	public long longValue(World world, Random rand, DataHolder data) {

		long min = this.min.longValue(world, rand, data);
		return getRandomLong(max.longValue(world, rand, data) - min, rand) + min;
	}

	@Override
	public double doubleValue(World world, Random rand, DataHolder data) {

		double min = this.min.doubleValue(world, rand, data);
		return getRandomDouble(max.doubleValue(world, rand, data) - min, rand) + min;
	}

}
