package cofh.cofhworld.data.condition;

import cofh.cofhworld.data.DataHolder;
import net.minecraft.world.IWorld;

import java.util.Random;

public interface ICondition {

	boolean checkCondition(IWorld world, Random rand, DataHolder data);
}
