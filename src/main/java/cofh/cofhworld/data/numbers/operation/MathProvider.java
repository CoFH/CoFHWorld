package cofh.cofhworld.data.numbers.operation;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.INumberProvider;
import net.minecraft.world.IWorld;

import java.util.Locale;
import java.util.Random;
import java.util.function.DoubleBinaryOperator;
import java.util.function.LongBinaryOperator;

public class MathProvider implements INumberProvider {

	protected final INumberProvider valueA;
	protected final INumberProvider valueB;
	protected final LongBinaryOperator longValue;
	protected final DoubleBinaryOperator doubleValue;

	public MathProvider(INumberProvider valueA, INumberProvider valueB, String type) {

		this.valueA = valueA;
		this.valueB = valueB;
		Operation operation = Operation.valueOf(type.toUpperCase(Locale.US));
		longValue = operation.longValue;
		doubleValue = operation.doubleValue;
	}

	public long longValue(IWorld world, Random rand, DataHolder data) {

		return longValue.applyAsLong(valueA.longValue(world, rand, data), valueB.longValue(world, rand, data));
	}

	public double doubleValue(IWorld world, Random rand, DataHolder data) {

		return doubleValue.applyAsDouble(valueA.doubleValue(world, rand, data), valueB.doubleValue(world, rand, data));
	}

	private static enum Operation {

		ADD(Long::sum, Double::sum),
		SUBTRACT((a, b) -> a - b, (a, b) -> a - b),
		MULTIPLY((a, b) -> a * b, (a, b) -> a * b),
		DIVIDE((a, b) -> a / b, (a, b) -> a / b),
		MODULO((a, b) -> a % b, (a, b) -> a % b),
		INVERSE_MODULO((a, b) -> a - (a % b), (a, b) -> a - (a % b)),
		POWER((a, b) -> (long)Math.pow(a, b), Math::pow),
		MINIMUM(Math::min, Math::min),
		MAXIMUM(Math::max, Math::max);

		private Operation(LongBinaryOperator longFunc, DoubleBinaryOperator doubleFunc) {

			longValue = longFunc;
			doubleValue = doubleFunc;
		}

		public final LongBinaryOperator longValue;

		public final DoubleBinaryOperator doubleValue;
	}

}
