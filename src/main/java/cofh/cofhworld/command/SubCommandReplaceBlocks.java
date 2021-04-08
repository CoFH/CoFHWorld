package cofh.cofhworld.command;

import cofh.cofhworld.command.Helpers.CoordinateHelpers;
import cofh.cofhworld.command.Helpers.FormatHelpers;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.inventory.IClearable;
import net.minecraft.nbt.INBT;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.server.ServerWorld;

import java.text.NumberFormat;
import java.util.function.Predicate;

import static cofh.cofhworld.command.Helpers.FilteredAreaArgumentHelpers.gatherArguments;

public class SubCommandReplaceBlocks {

	private static final int permissionLevel = 3;

	public static ArgumentBuilder<CommandSource, ?> register() {

		return Commands.literal("replaceblocks")
				.requires(source -> source.hasPermissionLevel(permissionLevel))
				.then(
						gatherArguments(Commands.argument("replacement", BlockStateArgument.blockState()), SubCommandReplaceBlocks::execute)
				);
	}

	private static int execute(CommandContext<CommandSource> context, BlockPos start, BlockPos end, Predicate<CachedBlockInfo> filters, Boolean wholeChunks) {

		replaceBlocks(context.getSource(), start, end, filters, BlockStateArgument.getBlockState(context, "replacement").getState(), wholeChunks);
		return Command.SINGLE_SUCCESS;
	}

	private static void replaceBlocks(CommandSource source, BlockPos start, BlockPos end, Predicate<CachedBlockInfo> filters, BlockState replacement, boolean wholeChunks) {

		ServerWorld serverworld = source.getWorld();

		long totalBlocks, replacedBlocks = 0;

		MutableBoundingBox area = CoordinateHelpers.coordinatesToBox(serverworld, start, end, wholeChunks);
		totalBlocks = area.getXSize() * area.getYSize() * area.getZSize();

		NumberFormat fmt = NumberFormat.getInstance();
		if (totalBlocks > 25_600)
			source.sendFeedback(FormatHelpers.getTranslationWithFormatting("cofhworld.replaceblocks.lag_warning",
					FormatHelpers.getStringWithFormatting(fmt.format(totalBlocks), INBT.SYNTAX_HIGHLIGHTING_NUMBER)), true);

		for (BlockPos pos : BlockPos.getAllInBoxMutable(area.minX, area.minY, area.minZ, area.maxX, area.maxY, area.maxZ)) {
			CachedBlockInfo cachedblockinfo = new CachedBlockInfo(serverworld, pos, true);
			if (filters.test(cachedblockinfo) && cachedblockinfo.getBlockState() != replacement) {
				IClearable.clearObj(cachedblockinfo.getTileEntity());

				// A value of 2 here will send this block change to all clients.
				// A value of 16 will prevent neighbour reactions.
				if (serverworld.setBlockState(pos, replacement, (2 | 16))) {
					++replacedBlocks;
				}
			}
		}

		source.sendFeedback(FormatHelpers.getTranslationWithFormatting("cofhworld.replaceblocks.successful",
				FormatHelpers.getStringWithFormatting(fmt.format(totalBlocks), INBT.SYNTAX_HIGHLIGHTING_NUMBER),
				FormatHelpers.getStringWithFormatting(fmt.format(replacedBlocks), INBT.SYNTAX_HIGHLIGHTING_NUMBER)), true);
	}

}
