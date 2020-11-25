package cofh.cofhworld.data.condition;

import cofh.cofhworld.data.DataHolder;
import net.minecraft.world.IWorld;

import java.util.Random;

public class ConstantCondition implements ICondition {

	final public static ICondition TRUE = new ConstantCondition(true), FALSE = new ConstantCondition(false);

	private boolean value;

	public ConstantCondition(boolean value) {

		this.value = value;
	}

	@Override
	public boolean checkCondition(IWorld world, Random rand, DataHolder data) {

		return value;
	}
}
