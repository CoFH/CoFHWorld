package cofh.cofhworld.util.random;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.WeightedRandom;

public class WeightedNBTTag extends WeightedRandom.Item {

	public final NBTBase tag;

	public WeightedNBTTag(NBTBase tag) {

		this(100, tag);
	}

	public WeightedNBTTag(int weight, NBTBase tag) {

		super(weight);
		this.tag = tag;
	}

	public NBTTagCompound getCompoundTag() {

		return (NBTTagCompound) tag;
	}

}
