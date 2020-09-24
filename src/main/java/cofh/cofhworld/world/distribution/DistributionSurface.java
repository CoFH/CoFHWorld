package cofh.cofhworld.world.distribution;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.util.Utils;
import cofh.cofhworld.world.generator.WorldGen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

import static cofh.cofhworld.world.generator.WorldGenMinableCluster.canGenerateInBlock;

public class DistributionSurface extends Distribution {

	private final WorldGen worldGen;
	private final INumberProvider count;
	private final Material[] matList;

	public DistributionSurface(String name, WorldGen worldGen, List<Material> matList, INumberProvider count, boolean regen) {

		super(name, regen);
		this.worldGen = worldGen;
		this.count = count;
		this.matList = matList.toArray(new Material[0]);
	}

	@Override
	public boolean generateFeature(Random random, int blockX, int blockZ, IWorld world) {

		BlockPos pos = new BlockPos(blockX, 64, blockZ);

		final int count = this.count.intValue(world, random, new DataHolder(pos));

		worldGen.setDecorationDefaults();

		boolean generated = false;
		for (int i = 0; i < count; i++) {
			int x = blockX + random.nextInt(16);
			int z = blockZ + random.nextInt(16);
			if (!canGenerateInBiome(world, x, z, random)) {
				continue;
			}

			int y = Utils.getSurfaceBlockY(world, x, z);

			if (!canGenerateInBlock(world, x, y, z, matList)) {
				continue;
			}

			generated |= worldGen.generate(world, random, new BlockPos(x, y + 1, z));
		}
		return generated;
	}

}
