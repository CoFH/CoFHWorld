package cofh.cofhworld.data.numbers.operation;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.INumberProvider;
import net.minecraft.world.IWorldReader;

import java.util.Locale;
import java.util.Random;

public class UnaryMathProvider implements INumberProvider {

	protected final INumberProvider value;
	protected final Operation operation;

	public UnaryMathProvider(INumberProvider valueA, String type) {

		this.value = valueA;
		this.operation = Operation.valueOf(type.toUpperCase(Locale.US));
	}

	public long longValue(IWorldReader world, Random rand, DataHolder data) {

		return operation.perform(value.longValue(world, rand, data));
	}

	public double doubleValue(IWorldReader world, Random rand, DataHolder data) {

		return operation.perform(value.doubleValue(world, rand, data));
	}

	private static enum Operation { // TODO: more operations before parsing

		ABSOLUTE {
			@Override
			public long perform(long a) {

				return Math.abs(a);
			}

			@Override
			public double perform(double a) {

				return Math.abs(a);
			}
		}, NEGATE {
			@Override
			public long perform(long a) {

				return -a;
			}

			@Override
			public double perform(double a) {

				return -a;
			}
		}, INCREMENT {
			@Override
			public long perform(long a) {

				return a + 1;
			}

			@Override
			public double perform(double a) {

				return a + 1;
			}
		}, DOUBLE {
			@Override
			public long perform(long a) {

				return a * 2;
			}

			@Override
			public double perform(double a) {

				return a * 2;
			}
		}, HALF {
			@Override
			public long perform(long a) {

				return a / 2;
			}

			@Override
			public double perform(double a) {

				return  a / 2;
			}
		}, SQUARE {
			@Override
			public long perform(long a) {

				return a * a;
			}

			@Override
			public double perform(double a) {

				return a * a;
			}
		}, SQUARE_ROOT {
			@Override
			public long perform(long a) {

				return (long) Math.sqrt(a);
			}

			@Override
			public double perform(double a) {

				return Math.sqrt(a);
			}
		}, CUBE {
			@Override
			public long perform(long a) {

				return a * a * a;
			}

			@Override
			public double perform(double a) {

				return a * a * a;
			}
		}, CUBE_ROOT {
			@Override
			public long perform(long a) {

				return (long) Math.cbrt(a);
			}

			@Override
			public double perform(double a) {

				return Math.cbrt(a);
			}
		};

		public abstract long perform(long a);

		public abstract double perform(double a);
	}

}
