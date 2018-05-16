package cofh.cofhworld.util;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.WeightedRandom;

public class WeightedRandomNBTTag extends WeightedRandom.Item {

	public final NBTBase tag;

	public WeightedRandomNBTTag(int weight, NBTBase tag) {

		super(weight);
		this.tag = tag;
	}

	public NBTTagCompound getCompoundTag() {

		return (NBTTagCompound) tag;
	}

}
