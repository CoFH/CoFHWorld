package cofh.cofhworld.data.condition;

import cofh.cofhworld.data.DataHolder;
import net.minecraft.world.World;

import java.util.Random;

public interface ICondition {

	boolean checkCondition(World world, Random rand, DataHolder data);
}
