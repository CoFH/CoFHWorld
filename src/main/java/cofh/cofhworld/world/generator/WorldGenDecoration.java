package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.condition.ConstantCondition;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.condition.world.WorldValueCondition;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.random.SkellamRandomProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * @deprecated TODO: split position variance logic out into something more base so all generators have the logic
 */
@Deprecated
public class WorldGenDecoration extends WorldGen {

	private static final ICondition SEE_SKY = new WorldValueCondition("CAN_SEE_SKY"), CHECK_STAY = new WorldValueCondition("BLOCK_CAN_PLACE");

	private final List<WeightedBlock> cluster;
	private final WeightedBlock[] material;
	private final WeightedBlock[] onBlock;
	private final INumberProvider clusterSize;
	private ICondition seeSky, checkStay;
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
		this.material = material == null ? null : material.toArray(new WeightedBlock[0]);
		onBlock = on == null ? null : on.toArray(new WeightedBlock[0]);
		this.setStackHeight(1).setSeeSky(SEE_SKY).setCheckStay(CHECK_STAY);
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
		for (int l = clusterSize, tries = 0; l-- > 0; ) {
			int x = xStart + xVar.intValue(world, rand, data.setPosition(start));
			int z = zStart + zVar.intValue(world, rand, data.setPosition(start.add(x - xStart, 0, 0)));
			int y = yStart + yVar.intValue(world, rand, data.setPosition(start.add(x - xStart, 0, z - zStart)));
			BlockPos pos = new BlockPos(x, y, z);

			if (!world.isBlockLoaded(pos)) {
				++l;
				if (++tries > 256) {
					break; // yeah, okay. provided values are somewhere not loaded, we're not just 'missing' at the edges
				}
				continue;
			}

			WeightedBlock block = selectBlock(rand, cluster);
			if (seeSky.checkCondition(world, rand, data.setPosition(pos).setBlock(block)) &&
					canGenerateInBlock(world, x, y - 1, z, onBlock) && canGenerateInBlock(world, x, y, z, material)) {

				int stack = stackHeight.intValue(world, rand, data);
				do {
					if (checkStay.checkCondition(world, rand, data)) {
						r |= setBlock(world, rand, pos, block);
					} else {
						break;
					}
					++y;
					pos = pos.add(0, 1, 0);
					if (!canGenerateInBlock(world, x, y, z, material)) {
						break;
					}
				} while (--stack > 0);
			}
		}
		return r;
	}

	@Deprecated
	public WorldGenDecoration setSeeSky(boolean seeSky) {

		return setSeeSky(seeSky ? SEE_SKY : ConstantCondition.TRUE);
	}

	@Deprecated
	public WorldGenDecoration setCheckStay(boolean checkStay) {

		return setCheckStay(checkStay ? CHECK_STAY : ConstantCondition.TRUE);
	}

	public WorldGenDecoration setSeeSky(ICondition seeSky) {

		this.seeSky = seeSky;
		return this;
	}

	public WorldGenDecoration setCheckStay(ICondition checkStay) {

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

	@Deprecated
	public WorldGenDecoration setXVar(int xVar) {

		return this.setXVar(new ConstantProvider(xVar));
	}

	public WorldGenDecoration setXVar(INumberProvider xVar) {

		this.xVar = xVar;
		return this;
	}

	@Deprecated
	public WorldGenDecoration setYVar(int yVar) {

		return this.setYVar(new ConstantProvider(yVar));
	}

	public WorldGenDecoration setYVar(INumberProvider yVar) {

		this.yVar = yVar;
		return this;
	}

	@Deprecated
	public WorldGenDecoration setZVar(int zVar) {

		return this.setZVar(new ConstantProvider(zVar));
	}

	public WorldGenDecoration setZVar(INumberProvider zVar) {

		this.zVar = zVar;
		return this;
	}

}
