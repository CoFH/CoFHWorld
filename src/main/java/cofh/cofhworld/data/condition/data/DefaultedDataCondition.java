package cofh.cofhworld.data.condition.data;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.condition.ICondition;
import net.minecraft.world.IWorldReader;

import java.util.Random;

public class DefaultedDataCondition implements ICondition {

	final private String key;
	final private ICondition def;

	public DefaultedDataCondition(String key, ICondition def) {

		this.key = key;
		this.def = def;
	}

	@Override
	public boolean checkCondition(IWorldReader world, Random rand, DataHolder data) {

		return data.hasValue(key, Boolean.class) ? data.getBool(key) : def.checkCondition(world, rand, data);
	}
}
