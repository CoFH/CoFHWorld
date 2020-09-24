package cofh.cofhworld.data.numbers.data;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.INumberProvider;
import net.minecraft.world.IWorldReader;

import java.util.Random;

public class TableProvider implements INumberProvider {

	private final INumberProvider[] table;
	private final INumberProvider lookupValue;
	private final INumberProvider lesserValue;
	private final INumberProvider greaterValue;

	public TableProvider(INumberProvider[] table, INumberProvider lookupValue, INumberProvider lesserValue, INumberProvider greaterValue) {

		this.table = table;

		this.lookupValue = lookupValue;
		this.lesserValue = lesserValue;
		this.greaterValue = greaterValue;
	}

	@Override
	public long longValue(IWorldReader world, Random rand, DataHolder data) {

		int lookup = lookupValue.intValue(world, rand, data);
		if (lookup < 0) {
			return lesserValue.longValue(world, rand, data);
		} else if (lookup >= table.length) {
			return greaterValue.longValue(world, rand, data);
		}
		return table[lookup].longValue(world, rand, data);
	}
}
