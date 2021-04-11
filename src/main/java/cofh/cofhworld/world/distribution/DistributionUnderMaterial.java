package cofh.cofhworld.world.distribution;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.util.Utils;
import cofh.cofhworld.world.generator.WorldGen;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;

import java.util.List;
import java.util.Random;

public class DistributionUnderMaterial extends Distribution {

	private final WorldGen worldGen;
	private final INumberProvider count;
	private final Material[] matList;
	private final Material[] fluidList;

	public DistributionUnderMaterial(String name, WorldGen worldGen, List<Material> matList, List<Material> fluidList, INumberProvider count, boolean regen) {

		super(name, regen);
		this.worldGen = worldGen;
		this.count = count;
		this.matList = matList.toArray(new Material[0]);
		this.fluidList = fluidList == null ? null : fluidList.toArray(new Material[0]);
	}

	@Override
	public boolean generateFeature(Random random, int blockX, int blockZ, ISeedReader world) {

		BlockPos.Mutable pos = new BlockPos.Mutable(blockX, 64, blockZ);

		final int count = this.count.intValue(world, random, new DataHolder(pos));

		worldGen.setDecorationDefaults();

		boolean generated = false;
		for (int i = 0; i < count; i++) {
			int x = blockX + random.nextInt(16);
			int z = blockZ + random.nextInt(16);
			if (!canGenerateInBiome(world, x, z, random)) {
				continue;
			}

			int y = Utils.getHighestY(world, x, z);
			BlockState stateAbove, state = world.getBlockState(pos.setPos(x, y + 1, z));
			l: do {
				stateAbove = state;
				state = world.getBlockState(pos.setPos(x, y, z));
				if (fluidList != null) {
					for (Material mat : fluidList)
						if (!mat.test(stateAbove))
							continue l;
				} else {
					if (stateAbove.getMaterial() != net.minecraft.block.material.Material.WATER) {
						continue;
					}
				}
				if (matList.length == 0)
					break; // success
				else for (Material mat : matList)
					if (mat.test(state))
						break l;
			} while (y-- > 0);

			if (y >= 0) {
				generated |= worldGen.generate(world, random, pos);
			}
		}
		return generated;
	}

}
