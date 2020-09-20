package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.condition.ConstantCondition;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

public class WorldGenMinableLargeVein extends WorldGen {

	private final List<WeightedBlock> resource;
	private final WeightedBlock[] material;
	private final INumberProvider veinSize;
	private ICondition sparse, spindly;

	public WorldGenMinableLargeVein(List<WeightedBlock> resource, int clusterSize) {

		this(resource, clusterSize, Blocks.STONE);
	}

	public WorldGenMinableLargeVein(WeightedBlock resource, int clusterSize, Block block) {

		this(fabricateList(resource), clusterSize, block);
	}

	public WorldGenMinableLargeVein(List<WeightedBlock> resource, int clusterSize, Block block) {

		this(resource, clusterSize, fabricateList(block));
	}

	public WorldGenMinableLargeVein(List<WeightedBlock> resource, int clusterSize, List<WeightedBlock> block) {

		this(resource, clusterSize, block, true);
	}

	public WorldGenMinableLargeVein(List<WeightedBlock> resource, INumberProvider clusterSize, List<WeightedBlock> block) {

		this.resource = resource;
		veinSize = clusterSize;
		material = block.toArray(new WeightedBlock[0]);
		this.setSparse(ConstantCondition.TRUE);
	}

	public WorldGenMinableLargeVein(List<WeightedBlock> resource, int clusterSize, List<WeightedBlock> block, boolean sparze) {

		this(resource, new ConstantProvider(clusterSize), block, sparze);
	}

	public WorldGenMinableLargeVein(List<WeightedBlock> resource, INumberProvider clusterSize, List<WeightedBlock> block, boolean sparze) {

		this.resource = resource;
		veinSize = clusterSize;
		material = block.toArray(new WeightedBlock[0]);
		sparse = sparze ? ConstantCondition.TRUE : ConstantCondition.FALSE;
	}

	@Deprecated
	public WorldGenMinableLargeVein setSpindly(boolean spindly) {

		return setSpindly(spindly ? ConstantCondition.TRUE : ConstantCondition.FALSE);
	}

	public WorldGenMinableLargeVein setSpindly(ICondition spindly) {

		this.spindly = spindly;
		return this;
	}

	public WorldGenMinableLargeVein setSparse(ICondition sparse) {

		this.sparse = sparse;
		return this;
	}

	@Override
	public boolean generate(IWorld world, Random rand, BlockPos pos) {

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		final DataHolder data = new DataHolder(pos);

		final int veinSize = this.veinSize.intValue(world, rand, data);
		final int branchSize = 1 + (veinSize / 30);
		final int subBranchSize = 1 + (branchSize / 5);
		final boolean sparse = this.sparse.checkCondition(world, rand, data), spindly = this.spindly.checkCondition(world, rand, data);

		boolean r = false;
		for (int blocksVein = 0; blocksVein <= veinSize; ) {
			int posX = x;
			int posY = y;
			int posZ = z;

			int directionChange = rand.nextInt(6);

			int directionX1 = -rand.nextInt(2);
			int directionY1 = -rand.nextInt(2);
			int directionZ1 = -rand.nextInt(2);
			{ // random code block to circumvent eclipse freaking out on auto-indent with unsigned right shift
				directionX1 += ~directionX1 >>> 31;
				directionY1 += ~directionY1 >>> 31;
				directionZ1 += ~directionZ1 >>> 31;
			}

			for (int blocksBranch = 0; blocksBranch <= branchSize; ) {
				if (directionChange != 1) {
					posX += rand.nextInt(2) * directionX1;
				}
				if (directionChange != 2) {
					posY += rand.nextInt(2) * directionY1;
				}
				if (directionChange != 3) {
					posZ += rand.nextInt(2) * directionZ1;
				}

				if (rand.nextInt(3) == 0) {
					int posX2 = posX;
					int posY2 = posY;
					int posZ2 = posZ;

					int directionChange2 = rand.nextInt(6);

					int directionX2 = -rand.nextInt(2);
					int directionY2 = -rand.nextInt(2);
					int directionZ2 = -rand.nextInt(2);
					{ // freaking out does not occur here, for some reason. the number at the end of the variable?
						directionX2 += ~directionX2 >>> 31;
						directionY2 += ~directionY2 >>> 31;
						directionZ2 += ~directionZ2 >>> 31;
					}

					for (int blocksSubBranch = 0; blocksSubBranch <= subBranchSize; ) {
						if (directionChange2 != 0) {
							posX2 += rand.nextInt(2) * directionX2;
						}
						if (directionChange2 != 1) {
							posY2 += rand.nextInt(2) * directionY2;
						}
						if (directionChange2 != 2) {
							posZ2 += rand.nextInt(2) * directionZ2;
						}

						r |= generateBlock(world, rand, posX2, posY2, posZ2, material, resource);

						if (sparse) {
							blocksVein++;
							blocksBranch++;
						}
						blocksSubBranch++;
					}
				}

				r |= generateBlock(world, rand, posX, posY, posZ, material, resource);

				if (spindly) {
					blocksVein++;
				}
				blocksBranch++;
			}

			x = x + (rand.nextInt(3) - 1);
			y = y + (rand.nextInt(3) - 1);
			z = z + (rand.nextInt(3) - 1);
			if (!spindly) {
				blocksVein++;
			}
		}

		return r;
	}

}
