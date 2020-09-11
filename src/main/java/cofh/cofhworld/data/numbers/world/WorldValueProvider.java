package cofh.cofhworld.data.numbers.world;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.INumberProvider;
import net.minecraft.world.World;

import java.util.Locale;
import java.util.Random;

public class WorldValueProvider implements INumberProvider {

	protected final WorldValueEnum data;

	public WorldValueProvider(String type) {

		this.data = WorldValueEnum.valueOf(type.toUpperCase(Locale.US));
	}

	public long longValue(World world, Random rand, DataHolder data) {

		return this.data.getValue(world, rand, data);
	}

}
