package cofh.cofhworld.feature;

import cofh.cofhworld.util.WeightedRandomBlock;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public interface IDistribution {
    boolean apply(Feature f, Random rand, int chunkX, int chunkZ, World world);

    List<WeightedRandomBlock> defaultMaterials();
}
