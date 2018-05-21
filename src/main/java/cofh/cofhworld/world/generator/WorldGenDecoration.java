package cofh.cofhworld.world.generator;

import cofh.cofhworld.util.random.WeightedBlock;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.random.SkellamRandomProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.List;
import java.util.Random;

public class WorldGenDecoration extends WorldGenerator {

	private final List<WeightedBlock> cluster;
	private final WeightedBlock[] genBlock;
	private final WeightedBlock[] onBlock;
	private final INumberProvider clusterSize;
	private boolean seeSky = true;
	private boolean checkStay = true;
	private INumberProvider stackHeight;
	private INumberProvider xVar;
	private INumberProvider yVar;
	private INumberProvider zVar;

	public WorldGenDecoration(List<WeightedBlock> blocks, int count, List<WeightedBlock> material, List<WeightedBlock> on) {

		this(blocks, new ConstantProvider(count), material, on);
	}

	public WorldGenDecoration(List<WeightedBlock> blocks, INumberProvider count, List<WeightedBlock> material, List<WeightedBlock> on) {

		cluster = blocks;
		clusterSize = count;
		genBlock = material == null ? null : material.toArray(new WeightedBlock[material.size()]);
		onBlock = on == null ? null : on.toArray(new WeightedBlock[on.size()]);
		this.setStackHeight(1);
		this.setXVar(new SkellamRandomProvider(8));
		this.setYVar(new SkellamRandomProvider(4));
		this.setZVar(new SkellamRandomProvider(8));
	}

	@Override
	public boolean generate(World world, Random rand, BlockPos start) {

		int xStart = start.getX();
		int yStart = start.getY();
		int zStart = start.getZ();

		final int clusterSize = this.clusterSize.intValue(world, rand, start);

		boolean r = false;
		for (int l = clusterSize; l-- > 0; ) {
			int x = xStart + xVar.intValue(world, rand, start);
			int y = yStart + yVar.intValue(world, rand, start);
			int z = zStart + zVar.intValue(world, rand, start);
			BlockPos pos = new BlockPos(x, y, z);

			if (!world.isBlockLoaded(pos)) {
				++l;
				continue;
			}

			if ((!seeSky || world.canSeeSky(pos)) &&
					WorldGenMinableCluster.canGenerateInBlock(world, x, y - 1, z, onBlock) &&
					WorldGenMinableCluster.canGenerateInBlock(world, x, y, z, genBlock)) {

				WeightedBlock block = WorldGenMinableCluster.selectBlock(rand, cluster);
				int stack = stackHeight.intValue(world, rand, pos);
				do {
					if (!checkStay || (block.block.canPlaceBlockAt(world, pos))) {
						r |= WorldGenMinableCluster.setBlock(world, pos, block);
					} else {
						break;
					}
					++y;
					pos = pos.add(0, 1, 0);
					if (!WorldGenMinableCluster.canGenerateInBlock(world, x, y, z, genBlock)) {
						break;
					}
				} while (--stack > 0);
			}
		}
		return r;
	}

	public WorldGenDecoration setSeeSky(boolean seeSky) {

		this.seeSky = seeSky;
		return this;
	}

	public WorldGenDecoration setCheckStay(boolean checkStay) {

		this.checkStay = checkStay;
		return this;
	}

	public WorldGenDecoration setStackHeight(int stackHeight) {

		return this.setStackHeight(new ConstantProvider(stackHeight));
	}

	public WorldGenDecoration setStackHeight(INumberProvider stackHeight) {

		this.stackHeight = stackHeight;
		return this;
	}

	public WorldGenDecoration setXVar(int xVar) {

		return this.setXVar(new ConstantProvider(xVar));
	}

	public WorldGenDecoration setXVar(INumberProvider xVar) {

		this.xVar = xVar;
		return this;
	}

	public WorldGenDecoration setYVar(int yVar) {

		return this.setYVar(new ConstantProvider(yVar));
	}

	public WorldGenDecoration setYVar(INumberProvider yVar) {

		this.yVar = yVar;
		return this;
	}

	public WorldGenDecoration setZVar(int zVar) {

		return this.setZVar(new ConstantProvider(zVar));
	}

	public WorldGenDecoration setZVar(INumberProvider zVar) {

		this.zVar = zVar;
		return this;
	}

}
