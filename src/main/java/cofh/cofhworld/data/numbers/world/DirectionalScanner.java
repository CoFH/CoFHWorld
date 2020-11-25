package cofh.cofhworld.data.numbers.world;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.numbers.INumberProvider;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.Random;

public class DirectionalScanner implements INumberProvider {

	final private ICondition condition;
	final private Direction direction;
	final private INumberProvider limit;

	public DirectionalScanner(ICondition condition, Direction direction, INumberProvider limit) {

		this.condition = condition;

		this.direction = direction;
		this.limit = limit;
	}

	@Override
	public long longValue(IWorld world, Random rand, DataHolder data) {

		final BlockPos start = data.getPosition();
		final ICondition condition = this.condition;
		final Direction direction = this.direction;

		int value = 0;
		for (
				final int end = limit.intValue(world, rand, data), amt = direction.getAxisDirection() == AxisDirection.NEGATIVE ? -1 : 1;
				(value * amt) < end && condition.checkCondition(world, rand, data);
				value += amt
			) {
			data.setPosition(data.getPosition().offset(direction));
		}
		data.setPosition(start);
		return value;
	}
}
