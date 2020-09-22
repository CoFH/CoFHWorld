package cofh.cofhworld.world.distribution;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.world.generator.WorldGen;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

public class DistributionReplace extends Distribution {

    private final Material[] blockstates;
    private final List<WeightedBlock> replacement;

    public DistributionReplace(String featureName, boolean retrogen, List<Material> blockstates, List<WeightedBlock> replacement) {
        super(featureName, retrogen);

        this.blockstates = blockstates.toArray(new Material[0]);
        this.replacement = replacement;
    }

    @Override
    public boolean generateFeature(Random random, int blockX, int blockZ, IWorld world) {
        boolean generated = false;
        for (int x = blockX; x < blockX + 16; x++)
        for (int z = blockZ; z < blockZ + 16; z++) {
            if (!canGenerateInBiome(world, x, z, random)) {
                continue;
            }

            for (int y = 0; y < world.getHeight(); y++) {
                generated |= WorldGen.generateBlock(world, random, x, y, z, blockstates, replacement);
            }
        }
        return generated;
    }
}
