package cofh.cofhworld.world.distribution;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.world.generator.WorldGen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.Random;

public class DistributionGaussian extends Distribution {

	private final WorldGen worldGen;
	private final INumberProvider count;
	private final INumberProvider rolls;
	private final INumberProvider meanY;
	private final INumberProvider maxVar;

	public DistributionGaussian(String name, WorldGen worldGen, INumberProvider count, INumberProvider smoothness, INumberProvider meanY, INumberProvider maxVar, boolean regen) {

		super(name, regen);
		this.worldGen = worldGen;
		this.count = count;
		this.rolls = smoothness;
		this.meanY = meanY;
		this.maxVar = maxVar;
	}

	@Override
	public boolean generateFeature(Random random, int blockX, int blockZ, IWorld world) {

		BlockPos pos = new BlockPos(blockX, 64, blockZ);

		DataHolder data = new DataHolder(pos);

		final int count = this.count.intValue(world, random, data);
		data.setValue("cluster-count", count);

		worldGen.setDecorationDefaults();

		boolean generated = false;
		for (int i = 0; i < count; i++) {
			data.setValue("cluster-current", i);
			int x = blockX + random.nextInt(16);
			int z = blockZ + random.nextInt(16);
			pos = new BlockPos(x, 64, z);
			int y = this.meanY.intValue(world, random, data.setPosition(pos));
			pos = pos.add(0, y - pos.getY(), 0);
			final int maxVar = this.maxVar.intValue(world, random, data.setPosition(pos).setValue("center-height", y));
			data.setValue("spread", maxVar);
			if (maxVar > 1) {
				final int rolls = this.rolls.intValue(world, random, data);
				data.setValue("smoothness", rolls);
				for (int v = 0; v < rolls; ++v) {
					y += random.nextInt(maxVar);
				}
				y = Math.round(y - (maxVar * (rolls * .5f)));
			}
			if (!canGenerateInBiome(world, x, z, random)) {
				continue;
			}

			generated |= worldGen.generate(world, random, new BlockPos(x, y, z));
		}
		return generated;
	}

}
