package cofh.cofhworld.command;

import cofh.cofhworld.command.Helpers.CoordinateHelpers;
import cofh.cofhworld.command.Helpers.FormatHelpers;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.nbt.INBT;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static cofh.cofhworld.command.Helpers.FilteredAreaArgumentHelpers.gatherArguments;

public class SubCommandCountBlocks {

	private static final int permissionLevel = 3;

	public static ArgumentBuilder<CommandSource, ?> register() {

		return gatherArguments(Commands.literal("countblocks")
				.requires(source -> source.hasPermissionLevel(permissionLevel))
				.then(
						Commands.literal("$list")
								.then(Commands.argument("page", IntegerArgumentType.integer())
										.executes(ctx -> displayBreakdownPage(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "page")))
								)
								.executes(ctx -> displayBreakdownPage(ctx.getSource(), 0))
				), SubCommandCountBlocks::execute);
	}

	private static int execute(CommandContext<CommandSource> context, BlockPos start, BlockPos end, Predicate<CachedBlockInfo> filters, boolean wholeChunks) {

		countBlocks(context.getSource(), start, end, filters, wholeChunks);
		return Command.SINGLE_SUCCESS;
	}

	// TODO: move this variable into a file or something, per command source rather than global
	private static final Object2LongLinkedOpenHashMap<Block> blockCounts = new Object2LongLinkedOpenHashMap<>();

	private static void countBlocks(CommandSource source, BlockPos start, BlockPos end, Predicate<CachedBlockInfo> filters, boolean wholeChunks) {

		ServerWorld serverworld = source.getWorld();

		blockCounts.clear();
		long totalBlocks, matchedBlocks = 0;

		MutableBoundingBox area = CoordinateHelpers.coordinatesToBox(serverworld, start, end, wholeChunks);
		totalBlocks = area.getXSize() * area.getYSize() * area.getZSize();

		for (BlockPos pos : BlockPos.getAllInBoxMutable(area.minX, area.minY, area.minZ, area.maxX, area.maxY, area.maxZ)) {
			CachedBlockInfo cachedblockinfo = new CachedBlockInfo(serverworld, pos, true);
			if (filters.test(cachedblockinfo)) {
				blockCounts.addTo(cachedblockinfo.getBlockState().getBlock(), 1);
				++matchedBlocks;
			}
		}

		NumberFormat fmt = NumberFormat.getInstance();

		source.sendFeedback(FormatHelpers.getTranslationWithFormatting("cofhworld.countblocks.successful",
				FormatHelpers.getStringWithFormatting(fmt.format(totalBlocks), INBT.SYNTAX_HIGHLIGHTING_NUMBER),
				FormatHelpers.getStringWithFormatting(fmt.format(matchedBlocks), INBT.SYNTAX_HIGHLIGHTING_NUMBER)), true);
	}

	private static final int PAGE_SIZE = 8;

	private static int displayBreakdownPage(CommandSource source, int page) {

		List<Object2LongMap.Entry<Block>> sortedBlockCounts = buildSortedList();

		// The count blocks command has not been (successfully) executed.
		if (sortedBlockCounts.size() == 0) {
			source.sendFeedback(new TranslationTextComponent("cofhworld.countblocks.list.failed"), true);
			return 0;
		}

		int totalPages = (sortedBlockCounts.size() - 1) / PAGE_SIZE;
		if (--page > totalPages) {
			page = totalPages;
		} else if (page < 0) {
			page = 0;
		}

		source.sendFeedback(FormatHelpers.getTranslationWithFormatting("cofhworld.countblocks.list.pages",
				FormatHelpers.getStringWithFormatting(String.valueOf(page + 1), INBT.SYNTAX_HIGHLIGHTING_NUMBER),
				FormatHelpers.getStringWithFormatting(String.valueOf(totalPages + 1), INBT.SYNTAX_HIGHLIGHTING_NUMBER)
		), false);

		NumberFormat fmt = NumberFormat.getInstance();
		for (int i = page * PAGE_SIZE, e = Math.min(sortedBlockCounts.size(), i + PAGE_SIZE); i < e; ++i) {

			Object2LongMap.Entry<Block> pair = sortedBlockCounts.get(i);
			String block = pair.getKey().getTranslationKey();
			String blockCount = fmt.format(pair.getLongValue());

			source.sendFeedback(FormatHelpers.getTranslationWithFormatting("cofhworld.countblocks.list.entry",
					FormatHelpers.getTranslationWithFormatting(block, INBT.SYNTAX_HIGHLIGHTING_KEY),
					FormatHelpers.getStringWithFormatting(blockCount, INBT.SYNTAX_HIGHLIGHTING_NUMBER)
			), false);
		}

		return Command.SINGLE_SUCCESS;
	}

	private static List<Object2LongMap.Entry<Block>> buildSortedList() {

		List<Object2LongMap.Entry<Block>> ret = new ArrayList<>(blockCounts.object2LongEntrySet());
		ret.sort((l, r) -> Long.compare(r.getLongValue(), l.getLongValue()));
		return ret;
	}

}
