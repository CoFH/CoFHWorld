package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.random.SkellamRandomProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * @deprecated TODO: replace all booleans with ICondition
 */
@Deprecated
public class WorldGenDecoration extends WorldGen {

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

		DataHolder data = new DataHolder(start);

		final int clusterSize = this.clusterSize.intValue(world, rand, data);

		boolean r = false;
		for (int l = clusterSize; l-- > 0; ) {
			int x = xStart + xVar.intValue(world, rand, data.setPosition(start));
			int z = zStart + zVar.intValue(world, rand, data.setPosition(start.add(x - xStart, 0, 0)));
			int y = yStart + yVar.intValue(world, rand, data.setPosition(start.add(x - xStart, 0, z - zStart)));
			BlockPos pos = new BlockPos(x, y, z);

			if (!world.isBlockLoaded(pos)) {
				++l;
				continue;
			}

			if ((!seeSky || world.canSeeSky(pos)) && canGenerateInBlock(world, x, y - 1, z, onBlock) && canGenerateInBlock(world, x, y, z, genBlock)) {

				WeightedBlock block = selectBlock(rand, cluster);
				int stack = stackHeight.intValue(world, rand, data.setPosition(pos));
				do {
					if (!checkStay || (block.block.canPlaceBlockAt(world, pos))) {
						r |= setBlock(world, rand, pos, block);
					} else {
						break;
					}
					++y;
					pos = pos.add(0, 1, 0);
					if (!canGenerateInBlock(world, x, y, z, genBlock)) {
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
