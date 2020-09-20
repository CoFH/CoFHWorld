package cofh.cofhworld.util.random;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.WeightedRandom;

public class WeightedNBTTag extends WeightedRandom.Item {

	public final INBT tag;

	public WeightedNBTTag(INBT tag) {

		this(100, tag);
	}

	public WeightedNBTTag(int weight, INBT tag) {

		super(weight);
		this.tag = tag;
	}

	public CompoundNBT getCompoundTag() {

		return (CompoundNBT) tag;
	}

}
