package cofh.cofhworld.data.numbers.operation;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.INumberProvider;
import net.minecraft.world.IWorld;

import java.util.Locale;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;
import java.util.function.LongUnaryOperator;

public class UnaryMathProvider implements INumberProvider {

	protected final INumberProvider value;
	protected final LongUnaryOperator longValue;
	protected final DoubleUnaryOperator doubleValue;

	public UnaryMathProvider(INumberProvider valueA, String type) {

		this.value = valueA;
		Operation operation = Operation.valueOf(type.toUpperCase(Locale.US));
		longValue = operation.longValue;
		doubleValue = operation.doubleValue;
	}

	public long longValue(IWorld world, Random rand, DataHolder data) {

		return longValue.applyAsLong(value.longValue(world, rand, data));
	}

	public double doubleValue(IWorld world, Random rand, DataHolder data) {

		return doubleValue.applyAsDouble(value.doubleValue(world, rand, data));
	}

	private static enum Operation {

		ABSOLUTE(Math::abs, Math::abs),
		NEGATE(a -> -a, a -> -a),
		INCREMENT(a -> a + 1, a -> a + 1),
		DECREMENT(a -> a - 1, a -> a - 1),
		DOUBLE(a -> a + a, a -> a + a),
		HALF(a -> a / 2, a -> a / 2),
		SQUARE(a -> a * a, a -> a * a),
		SQUARE_ROOT(a -> (long)Math.sqrt(a), Math::sqrt),
		CUBE(a -> a * a * a, a -> a * a * a),
		CUBE_ROOT(a -> (long)Math.cbrt(a), Math::cbrt),
		EXP(a -> (long)Math.exp(a), Math::exp),
		LOG(a -> (long)Math.log(a), Math::log),
		LOG10(a -> (long)Math.log10(a), Math::log10),
		CEIL(a -> a, Math::ceil),
		ROUND(a -> a, Math::round),
		FLOOR(a -> a, Math::floor);

		private Operation(LongUnaryOperator longFunc, DoubleUnaryOperator doubleFunc) {

			longValue = longFunc;
			doubleValue = doubleFunc;
		}

		public final LongUnaryOperator longValue;

		public final DoubleUnaryOperator doubleValue;
	}

}
