package cofh.cofhworld.command;

import cofh.cofhworld.command.Helpers.*;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.text.NumberFormat;
import java.util.*;

public class SubCommandCountBlocks {

    private static final int permissionLevel = 3;
    private static final int pageSize = 8;

    private static final HashMap<BlockState, Long> blockCounts = new HashMap<>();
    private static List<Map.Entry<BlockState, Long>> sortedBlockCounts = new ArrayList<>();
    private static long totalBlocks = 0;
    private static long totalMatchedBlocks = 0;

    public static ArgumentBuilder<CommandSource, ?> register() {

        return Commands.literal("countblocks")
                .requires(source -> source.hasPermissionLevel(permissionLevel))
                .then(Commands.argument("e", EntityArgument.entity())
                        .then(Commands.argument("r1", IntegerArgumentType.integer())
                                .then(Commands.argument("r2", IntegerArgumentType.integer())
                                        .then(Commands.argument("r3", IntegerArgumentType.integer())
                                                .then(Commands.argument("filter", StringArgumentType.string())
                                                        .then(Commands.argument("whole_chunk_mode", BoolArgumentType.bool())
                                                                // r1 applied to x, r2 applied to y and r3 applied to z. Filter has been specified. Chunk mode specifier also specified.
                                                                .executes(ctx -> executeAtEntity(ctx.getSource(), ArgHelpers.getEntity(ctx, "e"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r2"), ArgHelpers.getInt(ctx, "r3"), ArgHelpers.getString(ctx, "filter"), ArgHelpers.getBool(ctx, "whole_chunk_mode")))
                                                        )
                                                        // r1 applied to x, r2 applied to y and r3 applied to z. Filter has been specified.
                                                        .executes(ctx -> executeAtEntity(ctx.getSource(), ArgHelpers.getEntity(ctx, "e"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r2"), ArgHelpers.getInt(ctx, "r3"), ArgHelpers.getString(ctx, "filter"), false))
                                                )
                                                // r1 applied to x, r2 applied to y and r3 applied to z.
                                                .executes(ctx -> executeAtEntity(ctx.getSource(), ArgHelpers.getEntity(ctx, "e"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r2"), ArgHelpers.getInt(ctx, "r3"), "*", false))
                                        )
                                        // r1 applied to x and z, r2 applied to y.
                                        .executes(ctx -> executeAtEntity(ctx.getSource(), ArgHelpers.getEntity(ctx, "e"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r2"), ArgHelpers.getInt(ctx, "r1"), "*", false))
                                )
                                // r1 applied to x, y and z.
                                .executes(ctx -> executeAtEntity(ctx.getSource(), ArgHelpers.getEntity(ctx, "e"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r1"), "*", false))
                        )
                )
                .then(Commands.argument("start", BlockPosArgument.blockPos())
                        .then(Commands.argument("end", BlockPosArgument.blockPos())
                                .then(Commands.argument("filter", StringArgumentType.string())
                                        .then(Commands.argument("whole_chunk_mode", BoolArgumentType.bool())
                                                // Full coordinates, filter and chunk mode selector specified.
                                                .executes(ctx -> execute(ctx.getSource(), ArgHelpers.getBlockPos(ctx, "start"), ArgHelpers.getBlockPos(ctx, "end"), ArgHelpers.getString(ctx, "filter"), ArgHelpers.getBool(ctx, "whole_chunk_mode")))
                                        )
                                        // Full coordinates with filter specified.
                                        .executes(ctx -> execute(ctx.getSource(), ArgHelpers.getBlockPos(ctx, "start"), ArgHelpers.getBlockPos(ctx, "end"), ArgHelpers.getString(ctx, "filter"), false))
                                )
                                // Full coordinates specified.
                                .executes(ctx -> execute(ctx.getSource(), ArgHelpers.getBlockPos(ctx, "start"), ArgHelpers.getBlockPos(ctx, "end"), "*", false))
                        )
                );
    }

    public static ArgumentBuilder<CommandSource, ?> registerPageList() {

        return Commands.literal("countblockslist")
                .requires(source -> source.hasPermissionLevel(permissionLevel))
                .then(Commands.argument("page", IntegerArgumentType.integer())
                        .executes(ctx -> displayBreakdownPage(ctx.getSource(), ArgHelpers.getInt(ctx, "page")))
                );
    }

    private static int displayBreakdownPage(CommandSource source, int page) {

        // The count blocks command has not been (successfully) executed.
        if (blockCounts.size() == 0) {
            source.sendFeedback(new TranslationTextComponent("cofhworld.countblockslist.failed"), true);
            return 0;
        }

        if (sortedBlockCounts.size() == 0) {
            buildSortedList();
        }

        int totalPages = ((blockCounts.size() - 1) / pageSize) + 1;
        if (page > totalPages) {
            page = totalPages;
        } else if (page <= 0) {
            page = 1;
        }

        ArrayList<Pair<String, TextFormatting>> pageArgs = new ArrayList<>();
        pageArgs.add(Pair.of(String.valueOf(page), TextFormatting.GOLD));
        pageArgs.add(Pair.of(String.valueOf(totalPages), TextFormatting.GOLD));

        TranslationTextComponent pagesComponent = FormatHelpers.getTranslationWithFormatting("cofhworld.countblockslist.pages", pageArgs);
        source.sendFeedback(pagesComponent, true);

        NumberFormat fmt = NumberFormat.getInstance();
        int offset = (page - 1) * pageSize;
        for (int i = 0; i < pageSize; i++) {
            int index = i + offset;
            if (index >= sortedBlockCounts.size()) {
                break;
            }

            Map.Entry<BlockState, Long> pair = sortedBlockCounts.get(index);
            String block = pair.getKey().getBlock().getTranslatedName().getString();
            String blockCount = fmt.format(pair.getValue());

            ArrayList<Pair<String, TextFormatting>> entryArgs = new ArrayList<>();
            entryArgs.add(Pair.of(block, TextFormatting.BLUE));
            entryArgs.add(Pair.of(blockCount, TextFormatting.GOLD));

            TranslationTextComponent entryComponent = FormatHelpers.getTranslationWithFormatting("cofhworld.countblockslist.entry", entryArgs);
            source.sendFeedback(entryComponent, true);
        }

        return 1;
    }

    private static int executeAtEntity(CommandSource source, Entity entity, int xRadius, int yRadius, int zRadius, String filters, boolean wholeChunks) {

        BlockPos p = entity.getPosition();

        int x1 = p.getX() - xRadius;
        int y1 = p.getY() - yRadius;
        int z1 = p.getZ() - zRadius;
        int x2 = p.getX() + xRadius;
        int y2 = p.getY() + yRadius;
        int z2 = p.getZ() + zRadius;

        return execute(source, new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2), filters, wholeChunks);
    }

    private static int execute(CommandSource source, BlockPos start, BlockPos end, String filters, boolean wholeChunks) {

        int rtn = countBlocks(source, start, end, filters, wholeChunks);

        TextComponent component;
        if (rtn == -1) {
            component = new TranslationTextComponent("cofhworld.countblocks.failed");
        } else {
            NumberFormat fmt = NumberFormat.getInstance();

            ArrayList<Pair<String, TextFormatting>> args = new ArrayList<>();
            args.add(Pair.of(fmt.format(totalBlocks), TextFormatting.GOLD));
            args.add(Pair.of(fmt.format(totalMatchedBlocks), TextFormatting.GOLD));

            component = FormatHelpers.getTranslationWithFormatting("cofhworld.countblocks.successful", args);
        }

        source.sendFeedback(component, true);

        return rtn;
    }

    private static int countBlocks(CommandSource source, BlockPos start, BlockPos end, String filters, boolean wholeChunks) {

        Entity entity = source.getEntity();
        if (entity == null) {
            return 0;
        }

        World world = entity.getEntityWorld();
        if (world.isRemote) {
            return 0;
        }

        blockCounts.clear();
        sortedBlockCounts.clear();
        totalBlocks = 0;
        totalMatchedBlocks = 0;

        BlockFilters blockFilters = new BlockFilters(filters);
        MutableBoundingBox area = CoordinateHelpers.coordinatesToBox(world, start, end, wholeChunks);
        for (BlockPos pos : BlockPos.getAllInBoxMutable(area.minX, area.minY, area.minZ, area.maxX, area.maxY, area.maxZ)) {
            BlockState defaultState = world.getBlockState(pos).getBlock().getDefaultState();
            if (blockFilters.isFilterMatch(defaultState)) {
                long ofTypeCount = 1;
                if (blockCounts.containsKey(defaultState)) {
                    ofTypeCount = blockCounts.get(defaultState) + 1;
                }

                blockCounts.put(defaultState, ofTypeCount);
                ++totalMatchedBlocks;
            }

            ++totalBlocks;
        }

        return 1;
    }

    private static void buildSortedList() {

        sortedBlockCounts = new ArrayList<>(blockCounts.entrySet());
        sortedBlockCounts.sort(blockCountComparator);
    }

    private static final Comparator<Map.Entry<BlockState, Long>> blockCountComparator = (p1, p2) ->
            (p1.getValue() > p2.getValue() ? -1 : (p1.getValue().equals(p2.getValue()) ? 0 : 1));

}
