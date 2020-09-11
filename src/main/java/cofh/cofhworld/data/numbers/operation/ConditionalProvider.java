package cofh.cofhworld.data.numbers.operation;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.numbers.INumberProvider;
import net.minecraft.world.World;

import java.util.Random;

public class ConditionalProvider implements INumberProvider {

	private ICondition condition;
	private INumberProvider valueA;
	private INumberProvider valueB;

	public ConditionalProvider(ICondition condition, INumberProvider ifTrue, INumberProvider ifFalse) {

		this.condition = condition;
		this.valueA = ifTrue;
		this.valueB = ifFalse;
	}

	@Override
	public long longValue(World world, Random rand, DataHolder data) {

		return (condition.checkCondition(world, rand, data) ? valueA : valueB).longValue(world, rand, data);
	}

	@Override
	public double doubleValue(World world, Random rand, DataHolder data) {

		return (condition.checkCondition(world, rand, data) ? valueA : valueB).doubleValue(world, rand, data);
	}
}
