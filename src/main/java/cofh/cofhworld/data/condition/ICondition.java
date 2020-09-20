package cofh.cofhworld.data.condition;

import cofh.cofhworld.data.DataHolder;
import net.minecraft.world.IWorldReader;

import java.util.Random;

public interface ICondition {

	boolean checkCondition(IWorldReader world, Random rand, DataHolder data);
}
