package cofh.cofhworld.feature;

import net.minecraft.world.World;

import java.util.Random;

public interface IDistribution {
    boolean apply(Feature f, Random rand, int chunkX, int chunkZ, World world, IGenerator generator);
}
