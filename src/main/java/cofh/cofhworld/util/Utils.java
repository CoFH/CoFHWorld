package cofh.cofhworld.util;

import com.typesafe.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.Loader;

import java.io.*;
import java.nio.channels.FileChannel;

public class Utils {

	/* BLOCK UTILS */
	public static final int[][] SIDE_COORD_MOD = { { 0, -1, 0 }, { 0, 1, 0 }, { 0, 0, -1 }, { 0, 0, 1 }, { -1, 0, 0 }, { 1, 0, 0 } };

	public static int getHighestY(World world, int x, int z) {

		return world.getChunkFromBlockCoords(new BlockPos(x, 0, z)).getTopFilledSegment() + 16;
	}

	public static int getSurfaceBlockY(World world, int x, int z) {

		int y = world.getChunkFromBlockCoords(new BlockPos(x, 0, z)).getTopFilledSegment() + 16;

		BlockPos pos;
		IBlockState state;
		Block block;
		do {
			if (--y < 0) {
				break;
			}
			pos = new BlockPos(x, y, z);
			state = world.getBlockState(pos);
			block = state.getBlock();
		}
		while (block.isAir(state, world, pos) || block.isReplaceable(world, pos) || block.isLeaves(state, world, pos) || block.isFoliage(world, pos) || block.canBeReplacedByLeaves(state, world, pos));
		return y;
	}

	public static int getTopBlockY(World world, int x, int z) {

		int y = world.getChunkFromBlockCoords(new BlockPos(x, 0, z)).getTopFilledSegment() + 16;

		BlockPos pos;
		IBlockState state;
		Block block;
		do {
			if (--y < 0) {
				break;
			}
			pos = new BlockPos(x, y, z);
			state = world.getBlockState(pos);
			block = state.getBlock();
		} while (block.isAir(state, world, pos));
		return y;
	}

	/* FLUID UTILS */
	public static Fluid lookupFluidForBlock(Block block) {

		if (block == Blocks.FLOWING_WATER) {
			return FluidRegistry.WATER;
		}
		if (block == Blocks.FLOWING_LAVA) {
			return FluidRegistry.LAVA;
		}
		return FluidRegistry.lookupFluidForBlock(block);
	}

	/* ITEM UTILS */
	public static ItemStack cloneStack(ItemStack stack, int stackSize) {

		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack retStack = stack.copy();
		retStack.setCount(stackSize);

		return retStack;
	}

	public static boolean oreNameExists(String oreName) {

		return OreDictionary.doesOreNameExist(oreName);
	}

	/* FILE UTILS */
	public static void copyFileUsingStream(String source, String dest) throws IOException {

		copyFileUsingStream(source, new File(dest));
	}

	public static void copyFileUsingStream(String source, File dest) throws IOException {

		InputStream is = Loader.getResource(source, null).openStream();
		OutputStream os = new FileOutputStream(dest);
		byte[] buffer = new byte[1024];
		int length;
		while ((length = is.read(buffer)) > 0) {
			os.write(buffer, 0, length);
		}
	}

	public static void copyFileUsingChannel(File source, File dest) throws IOException {

		FileInputStream sourceStream = new FileInputStream(source);
		FileChannel sourceChannel = sourceStream.getChannel();
		FileOutputStream outputStream = new FileOutputStream(dest);
		outputStream.getChannel().transferFrom(sourceChannel, 0, sourceChannel.size());
	}

	public static boolean missingAnySetting(Config genObject, String featureName, Logger log, String... settings) {

		for (String settingName : settings) {
			if (!genObject.hasPath(settingName)) {
				log.error("Missing required setting {} on feature {}", settingName, featureName);
				return true;
			}
		}
		return false;
	}
}
