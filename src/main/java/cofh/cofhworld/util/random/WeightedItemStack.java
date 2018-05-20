package cofh.cofhworld.util.random;

import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;

public class WeightedItemStack extends WeightedRandom.Item {

	private final ItemStack stack;

	public WeightedItemStack(ItemStack stack) {

		this(stack, 100);
	}

	public WeightedItemStack(ItemStack stack, int weight) {

		super(weight);
		this.stack = stack;
	}

	public ItemStack getStack() {

		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		return stack.copy();
	}

}
