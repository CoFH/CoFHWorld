package cofh.cofhworld.data.condition.operation;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.condition.ICondition;
import net.minecraft.world.World;

import java.util.Random;

public class NotCondition implements ICondition {

	private ICondition value;

	public NotCondition(ICondition value) {

		this.value = value;
	}

	@Override
	public boolean checkCondition(World world, Random rand, DataHolder data) {

		return !value.checkCondition(world, rand, data);
	}
}
