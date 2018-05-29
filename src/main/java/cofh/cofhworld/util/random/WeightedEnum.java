package cofh.cofhworld.util.random;

import net.minecraft.util.WeightedRandom;

public class WeightedEnum<T extends Enum<T>> extends WeightedRandom.Item {

	public final T value;

	public WeightedEnum(T v) {

		this(v, 100);
	}

	public WeightedEnum(T v, int weight) {

		super(weight);
		this.value = v;
	}

}
