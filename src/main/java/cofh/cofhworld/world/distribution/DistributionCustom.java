package cofh.cofhworld.world.distribution;

import cofh.cofhworld.data.numbers.INumberProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public class DistributionCustom extends Distribution {

	private final WorldGenerator worldGen;
	private final INumberProvider count;
	private INumberProvider xPos;
	private INumberProvider zPos;
	private INumberProvider yPos;

	public DistributionCustom(String name, WorldGenerator worldGen, INumberProvider count, boolean regen, INumberProvider xPos, INumberProvider yPos, INumberProvider zPos) {

		super(name, regen);
		this.worldGen = worldGen;
		this.count = count;
		this.xPos = xPos;
		this.zPos = zPos;
		this.yPos = yPos;
	}

	@Override
	public boolean generateFeature(Random random, int blockX, int blockZ, World world) {

		BlockPos pos = new BlockPos(blockX, 64, blockZ);

		int e = count.intValue(world, random, pos);
		boolean r = false;
		for (int i = 0; i < e; ++i) {
			int x = xPos.intValue(world, random, pos);
			int z = zPos.intValue(world, random, pos.add(x, 0, 0));
			int y = yPos.intValue(world, random, pos.add(x, 0, z));
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
