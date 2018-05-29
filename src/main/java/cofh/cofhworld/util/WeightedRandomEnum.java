package cofh.cofhworld.util;

import net.minecraft.util.WeightedRandom;

public class WeightedRandomEnum<T extends Enum<T>> extends WeightedRandom.Item {

	public final T value;

	public WeightedRandomEnum(T v) {

		this(v, 100);
	}

	public WeightedRandomEnum(T v, int weight) {

		super(weight);
		this.value = v;
	}

}
