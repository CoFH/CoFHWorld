package cofh.cofhworld.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;

public class WeightedRandomString extends WeightedRandom.Item {

	public final String value;

	public WeightedRandomString(String s) {

		this(s, 100);
	}

	public WeightedRandomString(String s, int weight) {

		super(weight);
		this.value = s;
	}

}
