package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.DataHolder;
import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.data.numbers.ConstantProvider;
import cofh.cofhworld.data.numbers.INumberProvider;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class WorldGen {

	protected INumberProvider xOff = ConstantProvider.ZERO;
	protected INumberProvider yOff = ConstantProvider.ZERO;
	protected INumberProvider zOff = ConstantProvider.ZERO;

	final protected DataHolder getData(IWorld world, Random rand, BlockPos start) {

		DataHolder data = new DataHolder(start);
		return data.setPosition(getNewOffset(world, rand, data));
	}

	final protected BlockPos getNewOffset(IWorld world, Random rand, DataHolder data) {

		BlockPos start = data.getPos("start");

		int x = xOff.intValue(world, rand, data.setPosition(start));
		int z = zOff.intValue(world, rand, data.setPosition(start.add(x, 0, 0)));
		int y = yOff.intValue(world, rand, data.setPosition(start.add(x, 0, z)));
		return new BlockPos(start.getX() + x, MathHelper.clamp(start.getY() + y, 0, world.getMaxHeight()), start.getZ() + z);
	}

	final public boolean generate(IWorld world, Random rand, BlockPos position) {

		return this.generate(world, rand, getData(world, rand, position));
	}

	protected abstract boolean generate(IWorld world, Random rand, final DataHolder data);

	public void setDecorationDefaults() {

	}

	public WorldGen setOffsetX(INumberProvider xVar) {

		this.xOff = xVar;
		return this;
	}

	public WorldGen setOffsetY(INumberProvider yVar) {

		this.yOff = yVar;
		return this;
	}

	public WorldGen setOffsetZ(INumberProvider zVar) {

		this.zOff = zVar;
		return this;
	}

	public static List<WeightedBlock> fabricateList(WeightedBlock resource) {

		List<WeightedBlock> list = new ArrayList<>();
		list.add(resource);
		return list;
	}

	public static List<WeightedBlock> fabricateList(Block resource) {

		List<WeightedBlock> list = new ArrayList<>();
		list.add(new WeightedBlock(resource));
		return list;
	}

	public static boolean canGenerateInBlock(IWorld world, int x, int y, int z, Material[] mat) {

		return canGenerateInBlock(world, new BlockPos(x, y, z), mat);
	}

	public static boolean canGenerateInBlock(IWorldReader world, BlockPos pos, Material[] mat) {

		if (mat == null || mat.length == 0) {
			return true;
		}

		BlockState state = world.getBlockState(pos);
		for (int j = 0, e = mat.length; j < e; ++j) {
			Material material = mat[j];
			if (material.test(state)) {
				return true;
			}
		}
		return false;
	}

	public static boolean generateBlock(IWorld world, Random rand, int x, int y, int z, Material[] mat, List<WeightedBlock> o) {

		if (mat == null || mat.length == 0) {
			return generateBlock(world, rand, x, y, z, o);
		}

		if (canGenerateInBlock(world, x, y, z, mat)) {
			return generateBlock(world, rand, x, y, z, o);
		}
		return false;
	}

	public static boolean generateBlock(IWorld world, Random rand, int x, int y, int z, List<WeightedBlock> o) {

		return setBlock(world, rand, new BlockPos(x, y, z), selectBlock(rand, o));
	}

	public static boolean setBlock(IWorld world, Random rand, BlockPos pos, WeightedBlock ore) {

		if (ore != null && setBlockState(world, pos, ore.getState())) {
			if (ore.block.hasTileEntity(ore.getState())) {
				TileEntity tile = world.getTileEntity(pos);
				if (tile != null) {
					tile.deserializeNBT(ore.getData(rand, tile.serializeNBT()));
				}
			}
			return true;
		}
		return false;
	}

	public static boolean setBlockState(IWorld world, BlockPos pos, BlockState state) {

		boolean r = world.setBlockState(pos, state, 2 | 16);
		if (r && !state.getFluidState().isEmpty()) {
			world.getPendingFluidTicks().scheduleTick(pos, state.getFluidState().getFluid(), 0);
		}
		return r;
	}

	@Nullable
	public static WeightedBlock selectBlock(Random rand, List<WeightedBlock> o) {

		int size = o.size();
		if (size == 0) {
			return null;
		}
		if (size > 1) {
			return WeightedRandom.getRandomItem(rand, o);
		}
		return o.get(0);
	}

}
