package cofh.cofhworld.data.condition.operation;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.condition.ICondition;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.world.World;

import java.util.Locale;
import java.util.Random;

public class BinaryCondition implements ICondition {

	protected final ICondition valueA;
	protected final ICondition valueB;
	protected final Operation operation;

	public BinaryCondition(ICondition valueA, ICondition valueB, String type) {

		this.valueA = valueA;
		this.valueB = valueB;
		this.operation = Operation.getOperation(type.trim().toUpperCase(Locale.US));
	}

	@Override
	public boolean checkCondition(World world, Random rand, DataHolder data) {

		return operation.perform(valueA.checkCondition(world, rand, data), valueB.checkCondition(world, rand, data));
	}

	public static enum Operation {

		EQUAL_TO("==", "===", "equals") {
			@Override
			public boolean perform(boolean a, boolean b) {

				return a == b;
			}
		},
		NOT_EQUAL_TO("!=", "<>", "~=", "inequal", "unequal", "xor", "^") {
			@Override
			public boolean perform(boolean a, boolean b) {

				return a != b;
			}
		},
		AND("&", "&&") {
			@Override
			public boolean perform(boolean a, boolean b) {

				return a & b;
			}
		},
		OR("|", "||") {
			@Override
			public boolean perform(boolean a, boolean b) {

				return a | b;
			}
		},
		;

		private Operation(String... alts) {
			putEntry(name(), this);
			for (String name : alts)
				putEntry(name, this);
		}

		public abstract boolean perform(boolean a, boolean b);

		private static Object2ObjectArrayMap<String, Operation> mappings;

		private static void putEntry(String name, Operation value){

			if (mappings == null) {
				mappings = new Object2ObjectArrayMap<>();
			}
			mappings.put(name.toUpperCase(Locale.US).replace('_', '-'), value);
		}

		public static Operation getOperation(String type) {

			if (!mappings.containsKey(type))
				throw new IllegalArgumentException("No enum constant " + Operation.class.getCanonicalName() + "." + type);
			return mappings.get(type);
		}
	}
}
