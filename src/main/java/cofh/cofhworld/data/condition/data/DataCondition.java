package cofh.cofhworld.data.condition.data;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.condition.ICondition;
import net.minecraft.world.IWorld;

import java.util.Random;

public class DataCondition implements ICondition {

	final private String key;
	final private boolean check;

	public DataCondition(String key) {

		this.key = key;
		check = false;
	}

	public DataCondition(String key, boolean check) {

		this.key = key;
		this.check = check;
	}

	@Override
	public boolean checkCondition(IWorld world, Random rand, DataHolder data) {

		return check ? data.hasValue(key, Boolean.class) : data.getBool(key);
	}
}
