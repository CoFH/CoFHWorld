package cofh.cofhworld.data.numbers.operation;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.INumberProvider;
import net.minecraft.world.World;

import java.util.Locale;
import java.util.Random;

public class MathProvider implements INumberProvider {

	protected final INumberProvider valueA;
	protected final INumberProvider valueB;
	protected final Operation operation;

	public MathProvider(INumberProvider valueA, INumberProvider valueB, String type) {

		this.valueA = valueA;
		this.valueB = valueB;
		this.operation = Operation.valueOf(type.toUpperCase(Locale.US));
	}

	public long longValue(World world, Random rand, DataHolder data) {

		return operation.perform(valueA.longValue(world, rand, data), valueB.longValue(world, rand, data));
	}

	public double doubleValue(World world, Random rand, DataHolder data) {

		return operation.perform(valueA.doubleValue(world, rand, data), valueB.doubleValue(world, rand, data));
	}

	private static enum Operation {

		ADD {
			@Override
			public long perform(long a, long b) {

				return a + b;
			}

			@Override
			public double perform(double a, double b) {

				return a + b;
			}
		}, SUBTRACT {
			@Override
			public long perform(long a, long b) {

				return a - b;
			}

			@Override
			public double perform(double a, double b) {

				return a - b;
			}
		}, MULTIPLY {
			@Override
			public long perform(long a, long b) {

				return a * b;
			}

			@Override
			public double perform(double a, double b) {

				return a * b;
			}
		}, DIVIDE {
			@Override
			public long perform(long a, long b) {

				return a / b;
			}

			@Override
			public double perform(double a, double b) {

				return a / b;
			}
		}, MODULO {
			@Override
			public long perform(long a, long b) {

				return a % b;
			}

			@Override
			public double perform(double a, double b) {

				return a % b;
			}
		}, POWER {
			@Override
			public long perform(long a, long b) {

				return (long) perform((double) a, (double) b);
			}

			@Override
			public double perform(double a, double b) {

				return Math.pow(a, b);
			}
		}, MINIMUM {
			@Override
			public long perform(long a, long b) {

				return a <= b ? a : b;
			}

			@Override
			public double perform(double a, double b) {

				return a <= b ? a : b;
			}
		}, MAXIMUM {
			@Override
			public long perform(long a, long b) {

				return a >= b ? a : b;
			}

			@Override
			public double perform(double a, double b) {

				return a >= b ? a : b;
			}
		};

		public abstract long perform(long a, long b);

		public abstract double perform(double a, double b);
	}

}
