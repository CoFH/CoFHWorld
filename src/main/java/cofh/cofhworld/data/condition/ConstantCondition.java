package cofh.cofhworld.data.condition;

import cofh.cofhworld.data.DataHolder;
import net.minecraft.world.World;

import java.util.Random;

public class ConstantCondition implements ICondition{

	private boolean value;

	public ConstantCondition(boolean value) {

		this.value = value;
	}

	@Override
	public boolean checkCondition(World world, Random rand, DataHolder data) {

		return value;
	}
}
