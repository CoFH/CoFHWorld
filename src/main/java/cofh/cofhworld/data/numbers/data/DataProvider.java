package cofh.cofhworld.data.numbers.data;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.INumberProvider;
import net.minecraft.world.World;

import java.util.Locale;
import java.util.Random;

public class DataProvider implements INumberProvider {

	protected final String key;

	public DataProvider(String key) {

		this.key = key.toLowerCase(Locale.US);
	}

	@Override
	public long longValue(World world, Random rand, DataHolder data) {

		return data.getLong(key);
	}
}
