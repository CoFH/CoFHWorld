package cofh.cofhworld.feature;

import cofh.cofhworld.util.WeightedRandomBlock;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public interface IDistribution {
    boolean apply(Feature f, Random rand, int blockX, int blockZ, World world);

    List<WeightedRandomBlock> defaultMaterials();

    String defaultGenerator();
}
