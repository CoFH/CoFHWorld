package cofh.cofhworld.data.condition.data;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.condition.ICondition;
import net.minecraft.world.IWorld;

import java.util.Random;

public class CacheCondition extends DefaultedDataCondition {

	public CacheCondition(String key, ICondition value) {

		super("cache:" + key, value);
	}

	@Override
	public boolean checkCondition(IWorld world, Random rand, DataHolder data) {

		if (data.hasValue(key, Boolean.class)) return data.getBool(key);
		boolean value = def.checkCondition(world, rand, data);
		data.setValue(key, value);
		return value;
	}
}
