package cofh.cofhworld.data.numbers.random;

import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class SkellamRandomProvider implements INumberProvider {

	protected INumberProvider min;

	public SkellamRandomProvider(Number value) {

		min = new ConstantProvider(value);
	}

	public SkellamRandomProvider(INumberProvider value) {

		min = value;
	}

	public long longValue(World world, Random rand, BlockPos pos) {

		long val = min.longValue(world, rand, pos);
		return getRandomLong(val, rand) - getRandomLong(val, rand);
	}

	public double doubleValue(World world, Random rand, BlockPos pos) {

		double val = min.doubleValue(world, rand, pos);
		return getRandomDouble(val, rand) - getRandomDouble(val, rand);
	}

	public static long getRandomLong(long val, Random rand) {

		if (val == 0) {
			return 0;
		}
		int low = (int) (val & Integer.MAX_VALUE);
		int mid = (int) ((val >>> 31) & Integer.MAX_VALUE);
		int high = (int) ((val >>> 62) & Integer.MAX_VALUE);

		boolean mh = (mid | high) > 0;
		long r = mh ? rand.nextInt() & Integer.MAX_VALUE : rand.nextInt(low);
		if (mh) {
			r |= ((long) (high > 0 ? rand.nextInt() & Integer.MAX_VALUE : rand.nextInt(mid))) << 31;
		}
		if (high > 0) {
			r |= ((long) rand.nextInt(high)) << 62;
		}

		return r;
	}

	public static double getRandomDouble(double val, Random rand) {

		return rand.nextDouble() * val;
	}

}
