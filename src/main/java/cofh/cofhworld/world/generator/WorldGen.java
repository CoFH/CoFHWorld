package cofh.cofhworld.world.generator;

import cofh.cofhworld.data.block.Material;
import cofh.cofhworld.util.random.WeightedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class WorldGen {

	public abstract boolean generate(IWorld worldIn, Random rand, BlockPos position);

	public void setDecorationDefaults() {

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

	public static boolean canGenerateInBlock(IWorld world, BlockPos pos, Material[] mat) {

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

		if (ore != null && world.setBlockState(pos, ore.getState(), 2 | 16)) {
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
