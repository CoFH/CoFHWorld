package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.condition.ConstantCondition;
import cofh.cofhworld.data.condition.ICondition;
import cofh.cofhworld.data.condition.world.WorldValueCondition;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.data.numbers.random.SkellamRandomProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.List;
import java.util.Random;

public class WorldGenDecoration extends WorldGen {

	private static final ICondition SEE_SKY = new WorldValueCondition("CAN_SEE_SKY"), CHECK_STAY = new WorldValueCondition("BLOCK_CAN_PLACE");

	private final List<WeightedBlock> resource;
	private final Material[] material;
	private final Material[] surface;
	private final INumberProvider clusterSize;
	private ICondition seeSky, checkStay;
	private INumberProvider stackHeight;

	public WorldGenDecoration(List<WeightedBlock> blocks, INumberProvider count, List<Material> materials, List<Material> on) {

		resource = blocks;
		clusterSize = count;
		material = materials.toArray(new Material[0]);
		surface = on == null ? null : on.toArray(new Material[0]);
		this.setStackHeight(1).setSeeSky(SEE_SKY).setCheckStay(CHECK_STAY);
		this.setOffsetX(new SkellamRandomProvider(8));
		this.setOffsetY(new SkellamRandomProvider(4));
		this.setOffsetZ(new SkellamRandomProvider(8));
	}

	@Override
	public boolean generate(IWorld world, Random rand, final DataHolder data) {

		final int clusterSize = this.clusterSize.intValue(world, rand, data);
		data.setValue("quantity", clusterSize);

		boolean r = false;
		for (int l = clusterSize, tries = 0; l-- > 0; ) {
			BlockPos pos = data.setValue("quantity-current", l).getPosition();

			b: {
				if (!world.isBlockLoaded(pos)) {
					++l;
					if (++tries > 256) {
						break; // yeah, okay. provided values are somewhere not loaded, we're not just 'missing' at the edges
					}
					break b;
				}

				int x = pos.getX(), y = pos.getY(), z = pos.getZ();

				WeightedBlock block = selectBlock(rand, resource);
				if (seeSky.checkCondition(world, rand, data.setBlock(block)) &&
						canGenerateInBlock(world, x, y - 1, z, surface) && canGenerateInBlock(world, x, y, z, material)) {

					int stack = stackHeight.intValue(world, rand, data);
					do {
						if (checkStay.checkCondition(world, rand, data)) {
							r |= setBlock(world, rand, pos, block);
						} else {
							break;
						}
						++y;
						if (!canGenerateInBlock(world, x, y, z, material)) {
							break;
						}
						pos = pos.add(0, 1, 0);
					} while (--stack > 0);
				}
			}
			data.setPosition(getNewOffset(world, rand, data));
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

}
