package cofh.cofhworld.data.numbers.data;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.INumberProvider;
import net.minecraft.world.IWorld;

import java.util.Random;

public class CacheProvider extends DefaultedDataProvider {

	public CacheProvider(String key, INumberProvider value) {

		super("cache:" + key, value);
	}

	@Override
	public long longValue(IWorld world, Random rand, DataHolder data) {

		if (data.hasValue(key, Number.class)) return data.getLong(key);
		long value = def.longValue(world, rand, data);
		data.setValue(key, value);
		return value;
	}

	@Override
	public double doubleValue(IWorld world, Random rand, DataHolder data) {

		if (data.hasValue(key, Number.class)) return data.getDouble(key);
		double value = def.doubleValue(world, rand, data);
		data.setValue(key, value);
		return value;
	}
}
