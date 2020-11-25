package cofh.cofhworld.data.condition.operation;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.numbers.INumberProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.world.IWorld;

import java.util.Locale;
import java.util.Random;

public class ComparisonCondition implements ICondition {

	protected final INumberProvider valueA;
	protected final INumberProvider valueB;
	protected final Operation operation;

	public ComparisonCondition(INumberProvider valueA, INumberProvider valueB, String type) {

		this.valueA = valueA;
		this.valueB = valueB;
		this.operation = Operation.getOperation(type.trim().toUpperCase(Locale.US));
	}

	@Override
	public boolean checkCondition(IWorld world, Random rand, DataHolder data) {

		return operation.perform(valueA.longValue(world, rand, data), valueB.longValue(world, rand, data));
	}

	public static enum Operation {

		EQUAL_TO("==", "===", "equals", "equal") {
			@Override
			public boolean perform(long a, long b) {

				return a == b;
			}
		},
		NOT_EQUAL_TO("!=", "<>", "~=", "inequal", "unequal", "not_equal") {
			@Override
			public boolean perform(long a, long b) {

				return a != b;
			}
		},
		LESS_THAN("<") {
			@Override
			public boolean perform(long a, long b) {

				return a < b;
			}
		},
		GREATER_THAN(">") {
			@Override
			public boolean perform(long a, long b) {

				return a > b;
			}
		},
		LESS_THAN_OR_EQUAL_TO("<=", "less_than_or_equal") {
			@Override
			public boolean perform(long a, long b) {

				return a <= b;
			}
		},
		GREATER_THAN_OR_EQUAL_TO(">=", "greater_than_or_equal") {
			@Override
			public boolean perform(long a, long b) {

				return a >= b;
			}
		},
		;

		private Operation(String... alts) {
			putEntry(name(), this);
			for (String name : alts)
				putEntry(name, this);
		}

		public abstract boolean perform(long a, long b);

		private static Object2ObjectArrayMap<String, Operation> mappings;

		private static void putEntry(String name, Operation value){

			if (mappings == null) {
				mappings = new Object2ObjectArrayMap<>();
			}
			mappings.put(name.toUpperCase(Locale.US), value);
		}

		public static Operation getOperation(String type) {

			if (!mappings.containsKey(type))
				throw new IllegalArgumentException("No enum constant " + Operation.class.getCanonicalName() + "." + type);
			return mappings.get(type);
		}
	}
}
