package cofh.cofhworld.data.numbers.data;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.INumberProvider;
import net.minecraft.world.IWorld;

import java.util.Random;

public class StoreProvider extends DefaultedDataProvider {

	public StoreProvider(String key, INumberProvider value) {

		super("store:" + key, value);
	}

	@Override
	public long longValue(IWorld world, Random rand, DataHolder data) {

		long value = def.longValue(world, rand, data);
		data.setValue(key, value);
		return value;
	}

	@Override
	public double doubleValue(IWorld world, Random rand, DataHolder data) {

		double value = def.doubleValue(world, rand, data);
		data.setValue(key, value);
		return value;
	}
}

