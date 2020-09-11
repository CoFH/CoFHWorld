package cofh.cofhworld.data.condition.world;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.condition.ICondition;
import net.minecraft.world.World;

import java.util.Locale;
import java.util.Random;

public class WorldValueCondition implements ICondition {

	protected final WorldValueEnum data;

	public WorldValueCondition(String type) {

		this.data = WorldValueEnum.valueOf(type.toUpperCase(Locale.US));
	}

	@Override
	public boolean checkCondition(World world, Random rand, DataHolder data) {

		return this.data.getValue(world, rand, data);
	}
}
