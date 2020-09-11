package cofh.cofhworld.world.distribution;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.INumberProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public class DistributionUniform extends Distribution {

	private final WorldGenerator worldGen;
	private final INumberProvider count;
	private final INumberProvider minY;
	private final INumberProvider maxY;

	public DistributionUniform(String name, WorldGenerator worldGen, INumberProvider count, INumberProvider minY, INumberProvider maxY, boolean regen) {

		super(name, regen);
		this.worldGen = worldGen;
		this.count = count;
		this.minY = minY;
		this.maxY = maxY;
	}

	@Override
	public boolean generateFeature(Random random, int blockX, int blockZ, World world) {

		BlockPos pos = new BlockPos(blockX, 64, blockZ);

		DataHolder data = new DataHolder(pos);

		final int count = this.count.intValue(world, random, data);
		final int minY = Math.max(this.minY.intValue(world, random, data), 0), maxY = this.maxY.intValue(world, random, data);
		if (minY > maxY) {
			return false;
		}

		worldGen.setDecorationDefaults();

		boolean generated = false;
		for (int i = 0; i < count; i++) {
			int x = blockX + random.nextInt(16);
			int y = minY + (minY != maxY ? random.nextInt(maxY - minY) : 0);
			int z = blockZ + random.nextInt(16);
			if (!canGenerateInBiome(world, x, z, random)) {
				continue;
			}
			generated |= worldGen.generate(world, random, new BlockPos(x, y, z));
		}
		return generated;
	}

}
