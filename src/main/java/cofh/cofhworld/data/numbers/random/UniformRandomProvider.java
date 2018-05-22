package cofh.cofhworld.data.numbers.random;

import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import net.minecraft.util.math.BlockPos;
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
	public long longValue(World world, Random rand, BlockPos pos) {

		long min = this.min.longValue(world, rand, pos);
		return getRandomLong(max.longValue(world, rand, pos) - min, rand) + min;
	}

	@Override
	public double doubleValue(World world, Random rand, BlockPos pos) {

		double min = this.min.doubleValue(world, rand, pos);
		return getRandomDouble(max.doubleValue(world, rand, pos) - min, rand) + min;
	}

}
