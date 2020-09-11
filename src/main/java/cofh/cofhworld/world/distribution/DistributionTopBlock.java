package cofh.cofhworld.world.distribution;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.util.Utils;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.List;
import java.util.Random;

import static cofh.cofhworld.world.generator.WorldGenMinableCluster.canGenerateInBlock;

public class DistributionTopBlock extends Distribution {

	private final WorldGenerator worldGen;
	private final INumberProvider count;
	private final WeightedBlock[] matList;

	public DistributionTopBlock(String name, WorldGenerator worldGen, List<WeightedBlock> matList, INumberProvider count, boolean regen) {

		super(name, regen);
		this.worldGen = worldGen;
		this.count = count;
		this.matList = matList.toArray(new WeightedBlock[matList.size()]);
	}

	@Override
	public boolean generateFeature(Random random, int blockX, int blockZ, World world) {

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

			int y = Utils.getTopBlockY(world, x, z);
			l:
			{
				IBlockState state = world.getBlockState(new BlockPos(x, y, z));
				if (!state.getBlock().isAir(state, world, new BlockPos(x, y, z)) && canGenerateInBlock(world, x, y, z, matList)) {
					break l;
				}
				continue;
			}

			generated |= worldGen.generate(world, random, new BlockPos(x, y + 1, z));
		}
		return generated;
	}

}
