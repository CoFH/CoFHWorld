package cofh.cofhworld.data.numbers.data;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.INumberProvider;
import net.minecraft.world.IWorld;

import java.util.Locale;
import java.util.Random;

public class DefaultedDataProvider implements INumberProvider {

	protected final String key;
	protected final INumberProvider def;

	public DefaultedDataProvider(String key, INumberProvider def) {

		this.key = key.toLowerCase(Locale.US);
		this.def = def;
	}

	@Override
	public long longValue(IWorld world, Random rand, DataHolder data) {

		return data.hasValue(key, Number.class) ? data.getLong(key) : def.longValue(world, rand, data);
	}

	@Override
	public double doubleValue(IWorld world, Random rand, DataHolder data) {

		return data.hasValue(key, Number.class) ? data.getDouble(key) : def.doubleValue(world, rand, data);
	}
}
