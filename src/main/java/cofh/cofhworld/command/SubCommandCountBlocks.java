package cofh.cofhworld.command;

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
    private static ArrayList<Pair<Long, BlockState>> sortedBlockCounts = new ArrayList<>();
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
                                                    // r1 applied to x, r2 applied to y and r3 applied to z. Filter applied.
                                                    .executes(ctx -> executeAtEntity(ctx.getSource(), ArgHelpers.getEntity(ctx, "e"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r2"), ArgHelpers.getInt(ctx, "r3"), ArgHelpers.getString(ctx, "filter")))
                                                )
                                                // r1 applied to x, r2 applied to y and r3 applied to z. No filter specified, a match-all filter is used.
                                                .executes(ctx -> executeAtEntity(ctx.getSource(), ArgHelpers.getEntity(ctx, "e"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r2"), ArgHelpers.getInt(ctx, "r3"), "*"))
                                        )
                                        // r1 applied to x and z, r2 applied to y. No filter specified, a match-all filter is used.
                                        .executes(ctx -> executeAtEntity(ctx.getSource(), ArgHelpers.getEntity(ctx, "e"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r2"), ArgHelpers.getInt(ctx, "r1"), "*"))
                                )
                                // r1 applied to x, y and z. No filter specified, a match-all filter is used.
                                .executes(ctx -> executeAtEntity(ctx.getSource(), ArgHelpers.getEntity(ctx, "e"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r1"), "*"))
                        )
                )
                .then(Commands.argument("x1", BlockPosArgument.blockPos())
                        .then(Commands.argument("y1", BlockPosArgument.blockPos())
                                .then(Commands.argument("z1", BlockPosArgument.blockPos())
                                        .then(Commands.argument("x2", BlockPosArgument.blockPos())
                                                .then(Commands.argument("y2", BlockPosArgument.blockPos())
                                                        .then(Commands.argument("z1", BlockPosArgument.blockPos())
                                                                .then(Commands.argument("filter", StringArgumentType.string())
                                                                        // Full coordinates with filter specified.
                                                                        .executes(ctx -> execute(ctx.getSource(), ArgHelpers.getBlockPos(ctx, "x1"), ArgHelpers.getBlockPos(ctx, "y1"), ArgHelpers.getBlockPos(ctx, "z1"), ArgHelpers.getBlockPos(ctx, "x2"), ArgHelpers.getBlockPos(ctx, "y2"), ArgHelpers.getBlockPos(ctx, "z2"), ArgHelpers.getString(ctx, "filter")))
                                                                )
                                                                // Full coordinates specified. No filter specified, match-all filter applied as a default.
                                                                .executes(ctx -> execute(ctx.getSource(), ArgHelpers.getBlockPos(ctx, "x1"), ArgHelpers.getBlockPos(ctx, "y1"), ArgHelpers.getBlockPos(ctx, "z1"), ArgHelpers.getBlockPos(ctx, "x2"), ArgHelpers.getBlockPos(ctx, "y2"), ArgHelpers.getBlockPos(ctx, "z2"), "*"))
                                                        )
                                                )
                                        )
                                )
                        )
                );
    }

    public static ArgumentBuilder<CommandSource, ?> registerPageList() {

        return Commands.literal("countblockslist")
                .requires(source -> source.hasPermissionLevel(permissionLevel))
                .then(Commands.argument("page", IntegerArgumentType.integer())
                        .executes(ctx -> displayDetailedPage(ctx.getSource(), ArgHelpers.getInt(ctx, "page")))
                );
    }

    private static int displayDetailedPage(CommandSource source, int page) {

        // The count blocks command has not been (successfully) executed.
        if (blockCounts.size() == 0) {
            source.sendFeedback(new TranslationTextComponent("cofhworld.countblockslist.failed"), true);
            return 0;
        }

        if (sortedBlockCounts.size() == 0) {
            buildSortedList();
        }

        int totalPages = ((blockCounts.size() - 1) / pageSize) + 1;
        if  (page > totalPages) {
            page = totalPages;
        } else if (page <= 0) {
            page = 1;
        }

        source.sendFeedback(new TranslationTextComponent("cofhworld.countblockslist.pages", page, totalPages), true);

        NumberFormat fmt = NumberFormat.getInstance();

        int offset = (page - 1) * pageSize;
        for (int i = 0; i < pageSize; i++) {
            int index = i + offset;
            if (index >= sortedBlockCounts.size()) {
                break;
            }

            Pair<Long, BlockState> pair = sortedBlockCounts.get(index);
            String blockCount = fmt.format(pair.getLeft());
            IFormattableTextComponent block = pair.getRight().getBlock().getTranslatedName();
            source.sendFeedback(new TranslationTextComponent("cofhworld.countblockslist.entry", block, blockCount), true);
        }

        return 1;
    }

    private static int executeAtEntity(CommandSource source, Entity entity, int xRadius, int yRadius, int zRadius, String filters) {

        BlockPos p = entity.getPosition();

        int sX = p.getX() - xRadius;
        int sY = p.getY() - yRadius;
        int sZ = p.getZ() - zRadius;
        int eX = p.getX() + xRadius;
        int eY = p.getY() + yRadius;
        int eZ = p.getZ() + zRadius;

        return execute(source, sX, sY, sZ, eX, eY, eZ, filters);
    }

    private static int execute(CommandSource source, BlockPos sX, BlockPos sY, BlockPos sZ, BlockPos eX, BlockPos eY, BlockPos eZ, String filters) {

        return execute(source, sX.getX(), sY.getY(), sZ.getZ(), eX.getX(), eY.getY(), eZ.getZ(), filters);
    }

    private static int execute(CommandSource source, int sX, int sY, int sZ, int eX, int eY, int eZ, String filters) {

        int rtn = countBlocks(source, sX, sY, sZ, eX, eY, eZ, filters);

        TextComponent component;
        if (rtn == -1) {
            component = new TranslationTextComponent("cofhworld.countblocks.failed");
        } else {
            NumberFormat fmt = NumberFormat.getInstance();
            component = new TranslationTextComponent("cofhworld.countblocks.successful", fmt.format(totalBlocks), fmt.format(totalMatchedBlocks));
        }

        source.sendFeedback(component, true);

        return rtn;
    }

    private static int countBlocks(CommandSource source, int sX, int sY, int sZ, int eX, int eY, int eZ, String filters) {

        Entity entity = source.getEntity();
        if (entity == null) {
            return 0;
        }

        World world = entity.getEntityWorld();
        if (world.isRemote) {
            return 0;
        }

        // TODO - in 1.17 this should be modified to use the getBottomY and getTopY methods of the HeightLimitView interface.
        // The assumption that y == 0 is the lower bound of the world is not valid in 1.17.
        int maxY = world.getHeight();

        // Clamp y values to the world world height limits.
        // Do not use 256 here as it is variable in 1.17+
        if (sY < 0) {
            sY = 0;
        } else if (sY > maxY) {
            sY = maxY;
        }

        if (eY < 0) {
            eY = 0;
        } else if (sY > maxY) {
            eY = maxY;
        }

        BlockFilters blockFilters = new BlockFilters(filters);

        blockCounts.clear();
        sortedBlockCounts.clear();
        totalBlocks = 0;
        totalMatchedBlocks = 0;

        MutableBoundingBox area = MutableBoundingBox.createProper(sX, sY, sZ, eX, eY, eZ);
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

        ArrayList<Pair<Long, BlockState>> temp = new ArrayList<>();

        blockCounts.forEach((state, count) -> {
            temp.add(Pair.of(count, state));
        });

        // TODO: this will explode if there are a large number of entries present.
        // There might be a cleaner and more efficient way of doing this that will not explode.
        //Collections.sort(temp);
        //Collections.reverse(temp);

        sortedBlockCounts = temp;
    }
}
