package cofh.cofhworld.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.core.util.Loader;

import java.io.*;
import java.nio.channels.FileChannel;

public class Utils {

	/* BLOCK UTILS */
	public static final int[][] SIDE_COORD_MOD = { { 0, -1, 0 }, { 0, 1, 0 }, { 0, 0, -1 }, { 0, 0, 1 }, { -1, 0, 0 }, { 1, 0, 0 } };

	public static int getHighestY(World world, int x, int z) {

		return world.getChunkAt(new BlockPos(x, 0, z)).getTopFilledSegment() + 16;
	}

	public static int getSurfaceBlockY(World world, int x, int z) {

		int y = world.getChunkAt(new BlockPos(x, 0, z)).getTopFilledSegment() + 16;

		BlockPos pos;
		BlockState state;
		Block block;
		do {
			if (--y < 0) {
				break;
			}
			pos = new BlockPos(x, y, z);
			state = world.getBlockState(pos);
			block = state.getBlock();
		}
		// @formatter:off
		while (block.isAir(state, world, pos) ||
				state.getMaterial().isReplaceable() ||
				block.isIn(BlockTags.LOGS) ||
				block.isFoliage(state, world, pos) ||
				block.isIn(BlockTags.LEAVES)||
				block.canBeReplacedByLeaves(state, world, pos));
		// @formatter:on
		return y;
	}

	public static int getTopBlockY(World world, int x, int z) {

		int y = world.getChunkAt(new BlockPos(x, 0, z)).getTopFilledSegment() + 16;

		BlockPos pos;
		BlockState state;
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

		return Fluids.WATER; // FluidRegistry.lookupFluidForBlock(block);
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

}
