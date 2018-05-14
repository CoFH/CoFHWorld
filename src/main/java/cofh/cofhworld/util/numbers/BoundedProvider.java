package cofh.cofhworld.util.numbers;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BoundedProvider implements INumberProvider {

	protected final INumberProvider value;
	protected final INumberProvider min, max;

	public BoundedProvider(INumberProvider value, INumberProvider min, INumberProvider max) {

		this.value = value;
		this.min = min;
		this.max = max;
	}

	@Override
	public int intValue(World world, Random rand, BlockPos pos) {

		return (int) longValue(world, rand, pos);
	}

	@Override
	public long longValue(World world, Random rand, BlockPos pos) {

		return Math.min(Math.max(value.longValue(world, rand, pos), min.longValue(world, rand, pos)), max.longValue(world, rand, pos));
	}

	@Override
	public float floatValue(World world, Random rand, BlockPos pos) {

		return (float) doubleValue(world, rand, pos);
	}

	@Override
	public double doubleValue(World world, Random rand, BlockPos pos) {

		return Math.min(Math.max(value.doubleValue(world, rand, pos), min.doubleValue(world, rand, pos)), max.doubleValue(world, rand, pos));
	}

}
