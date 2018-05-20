package cofh.cofhworld.util.random;

import net.minecraft.util.WeightedRandom;

public class WeightedString extends WeightedRandom.Item {

	public final String value;

	public WeightedString(String s) {

		this(s, 100);
	}

	public WeightedString(String s, int weight) {

		super(weight);
		this.value = s;
	}

}
