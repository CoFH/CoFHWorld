package cofh.cofhworld.feature;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public interface IGenerator {

	boolean generate(Feature feature, World world, Random rand, BlockPos pos);
}
