package cofh.cofhworld.data.numbers.operation;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.random.SkellamRandomProvider;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;

import java.util.Random;

public class WorldHeightBoundProvider extends SkellamRandomProvider {

	public WorldHeightBoundProvider(INumberProvider value) {

		super(value);
	}

	@Override
	public long longValue(IWorld world, Random rand, DataHolder data) {

		return MathHelper.clamp(min.longValue(world, rand, data), 0, world.getHeight());
	}

	@Override
	public double doubleValue(IWorld world, Random rand, DataHolder data) {

		return MathHelper.clamp(min.doubleValue(world, rand, data), 0, world.getHeight());
	}
}
