package cofh.cofhworld.world.distribution;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.world.generator.WorldGen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;

import java.util.Random;

public class DistributionCustom extends Distribution {

	private final WorldGen worldGen;
	private final INumberProvider count;
	private INumberProvider xPos;
	private INumberProvider zPos;
	private INumberProvider yPos;

	public DistributionCustom(String name, WorldGen worldGen, INumberProvider count, boolean regen, INumberProvider xPos, INumberProvider yPos, INumberProvider zPos) {

		super(name, regen);
		this.worldGen = worldGen;
		this.count = count;
		this.xPos = xPos;
		this.zPos = zPos;
		this.yPos = yPos;
	}

	@Override
	public boolean generateFeature(Random random, int blockX, int blockZ, ISeedReader world) {

		BlockPos pos = new BlockPos(blockX, 64, blockZ);
		DataHolder data = new DataHolder(pos);

		worldGen.setDecorationDefaults();

		int e = count.intValue(world, random, data);
		data.setValue("cluster-count", e);
		boolean r = false;
		for (int i = 0; i < e; ++i) {
			data.setValue("cluster-current", i);
			int x = xPos.intValue(world, random, data.setPosition(pos));
			int z = zPos.intValue(world, random, data.setPosition(pos.add(x, 0, 0)));
			int y = yPos.intValue(world, random, data.setPosition(pos.add(x, 0, z)));
			x += blockX;
			z += blockZ;
			if (!canGenerateInBiome(world, x, z, random)) {
				continue;
			}
			r |= worldGen.generate(world, random, new BlockPos(x, y, z));
		}
		return r;
	}

}
