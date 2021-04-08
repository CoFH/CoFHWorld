package cofh.cofhworld.command;

import cofh.cofhworld.command.Helpers.CoordinateHelpers;
import cofh.cofhworld.command.Helpers.FormatHelpers;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.INBT;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.common.util.Constants.NBT;

import java.io.File;
import java.io.IOException;
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

	private static void countBlocks(CommandSource source, BlockPos start, BlockPos end, Predicate<CachedBlockInfo> filters, boolean wholeChunks) {

		ServerWorld serverworld = source.getWorld();

		CompoundNBT blockCounts = new CompoundNBT();
		long totalBlocks, matchedBlocks = 0;

		MutableBoundingBox area = CoordinateHelpers.coordinatesToBox(serverworld, start, end, wholeChunks);
		totalBlocks = area.getXSize() * area.getYSize() * area.getZSize();

		NumberFormat fmt = NumberFormat.getInstance();
		if (totalBlocks > 51_200)
			source.sendFeedback(FormatHelpers.getTranslationWithFormatting("cofhworld.countblocks.lag_warning",
					FormatHelpers.getStringWithFormatting(fmt.format(totalBlocks), INBT.SYNTAX_HIGHLIGHTING_NUMBER)), true);

		for (BlockPos pos : BlockPos.getAllInBoxMutable(area.minX, area.minY, area.minZ, area.maxX, area.maxY, area.maxZ)) {
			CachedBlockInfo cachedblockinfo = new CachedBlockInfo(serverworld, pos, true);
			if (filters.test(cachedblockinfo)) {
				String key = cachedblockinfo.getBlockState().getBlock().getTranslationKey();
				// BlockStateParser.toString(cachedblockinfo.getBlockState());
				blockCounts.putLong(key, blockCounts.getLong(key) + 1);
				++matchedBlocks;
			}
		}

		String resultKey = "cofhworld.countblocks.successful";
		try {
			File outFile = new File(source.getServer().func_240776_a_(CMD_STORAGE).toFile(), source.getName() + ".dat");
			if (!outFile.exists()) {
				outFile.getParentFile().mkdirs();
				outFile.createNewFile();
				//outFile.deleteOnExit();
			}
			{
				CompoundNBT wrapper = new CompoundNBT();
				wrapper.putLong("time", source.getWorld().getGameTime());
				wrapper.putString("world", source.getWorld().getDimensionKey().getLocation().toString());
				wrapper.put("blocks", blockCounts);
				wrapper.put("area", area.toNBTTagIntArray());
				wrapper.putLong("matched", matchedBlocks);
				CompressedStreamTools.writeCompressed(wrapper, outFile);
			}
		} catch (IOException ex) {
			// TODO: log error? needs separate command logger to avoid confusion with worldgen reloading logging
			resultKey = "cofhworld.countblocks.failed";
		}

		source.sendFeedback(FormatHelpers.getTranslationWithFormatting(resultKey,
				FormatHelpers.getStringWithFormatting(fmt.format(totalBlocks), INBT.SYNTAX_HIGHLIGHTING_NUMBER),
				FormatHelpers.getStringWithFormatting(fmt.format(matchedBlocks), INBT.SYNTAX_HIGHLIGHTING_NUMBER)), true);
	}

	private static final int PAGE_SIZE = 8;
	private static final FolderName CMD_STORAGE = new FolderName("generated/command/cofh/world/countblocks/");

	private static int displayBreakdownPage(CommandSource source, int page) {

		String worldLoc = "unknown";
		Object2LongLinkedOpenHashMap<String> blockCounts;
		try {
			File inFile = new File(source.getServer().func_240776_a_(CMD_STORAGE).toFile(), source.getName() + ".dat");
			{
				CompoundNBT wrapper = CompressedStreamTools.readCompressed(inFile);
				if (wrapper.contains("world", NBT.TAG_STRING))
					worldLoc = wrapper.getString("world");
				CompoundNBT blocks = wrapper.getCompound("blocks");
				blockCounts = new Object2LongLinkedOpenHashMap<>();
				for (String key : blocks.keySet())
					blockCounts.put(key, blocks.getLong(key));
			}
		} catch (IOException p) {
			source.sendFeedback(FormatHelpers.getTranslationWithFormatting("cofhworld.countblocks.list.failed"), true);
			return 0;
		}

		List<Object2LongMap.Entry<String>> sortedBlockCounts = buildSortedList(blockCounts);

		// The count blocks command has not been (successfully) executed.
		if (sortedBlockCounts.size() == 0) {
			source.sendFeedback(FormatHelpers.getTranslationWithFormatting("cofhworld.countblocks.list.failed"), true);
			return 0;
		}

		int totalPages = (sortedBlockCounts.size() - 1) / PAGE_SIZE;
		if (--page > totalPages) {
			page = totalPages;
		} else if (page < 0) {
			page = 0;
		}

		source.sendFeedback(FormatHelpers.getTranslationWithFormatting("cofhworld.countblocks.list.pages",
				FormatHelpers.getStringWithFormatting(String.valueOf(page + 1), INBT.SYNTAX_HIGHLIGHTING_NUMBER_TYPE),
				FormatHelpers.getStringWithFormatting(String.valueOf(totalPages + 1), INBT.SYNTAX_HIGHLIGHTING_NUMBER_TYPE),
				FormatHelpers.getStringWithFormatting(worldLoc, INBT.SYNTAX_HIGHLIGHTING_STRING)
		), false);

		NumberFormat fmt = NumberFormat.getInstance();
		for (int i = page * PAGE_SIZE, e = Math.min(sortedBlockCounts.size(), i + PAGE_SIZE); i < e; ++i) {

			Object2LongMap.Entry<String> pair = sortedBlockCounts.get(i);
			String block = pair.getKey();
			String blockCount = fmt.format(pair.getLongValue());

			source.sendFeedback(FormatHelpers.getTranslationWithFormatting("cofhworld.countblocks.list.entry",
					FormatHelpers.getTranslationWithFormatting(block, INBT.SYNTAX_HIGHLIGHTING_KEY),
					FormatHelpers.getStringWithFormatting(blockCount, INBT.SYNTAX_HIGHLIGHTING_NUMBER)
			), false);
		}

		return Command.SINGLE_SUCCESS;
	}

	private static List<Object2LongMap.Entry<String>> buildSortedList(Object2LongLinkedOpenHashMap<String> blockCounts) {

		List<Object2LongMap.Entry<String>> ret = new ArrayList<>(blockCounts.object2LongEntrySet());
		ret.sort((l, r) -> Long.compare(r.getLongValue(), l.getLongValue()));
		return ret;
	}

}
