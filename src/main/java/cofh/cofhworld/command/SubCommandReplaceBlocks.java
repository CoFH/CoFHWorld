package cofh.cofhworld.command;

import cofh.cofhworld.command.Helpers.*;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IClearable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.server.*;

import java.text.NumberFormat;
import java.util.*;

public class SubCommandReplaceBlocks {

    private static final int permissionLevel = 3;
    private static long totalBlocks = 0;
    private static long totalReplacedBlocks = 0;

    public static ArgumentBuilder<CommandSource, ?> register() {

        BlockStateInput air = new BlockStateInput(Blocks.AIR.getDefaultState(), Collections.emptySet(), null);

        return Commands.literal("replaceblocks")
                .requires(source -> source.hasPermissionLevel(permissionLevel))
                .then(Commands.argument("e", EntityArgument.entity())
                        .then(Commands.argument("r1", IntegerArgumentType.integer())
                                .then(Commands.argument("r2", IntegerArgumentType.integer())
                                        .then(Commands.argument("r3", IntegerArgumentType.integer())
                                                .then(Commands.argument("filter", StringArgumentType.string())
                                                        .then(Commands.argument("replacement", BlockStateArgument.blockState())
                                                                .then(Commands.argument("whole_chunk_mode", BoolArgumentType.bool())
                                                                        // r1 applied to x, r2 applied to y and r3 applied to z. Filter has been specified, as has the replacement block state. Chunk mode specifier also specified.
                                                                        .executes(ctx -> executeAtEntity(ctx.getSource(), ArgHelpers.getEntity(ctx, "e"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r2"), ArgHelpers.getInt(ctx, "r3"), ArgHelpers.getString(ctx, "filter"), ArgHelpers.getBlockState(ctx, "replacement"), ArgHelpers.getBool(ctx, "whole_chunk_mode")))
                                                                )
                                                                // r1 applied to x, r2 applied to y and r3 applied to z. Filter has been specified, as has the replacement block state.
                                                                .executes(ctx -> executeAtEntity(ctx.getSource(), ArgHelpers.getEntity(ctx, "e"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r2"), ArgHelpers.getInt(ctx, "r3"), ArgHelpers.getString(ctx, "filter"), ArgHelpers.getBlockState(ctx, "replacement"), false))
                                                        )
                                                        // r1 applied to x, r2 applied to y and r3 applied to z. Filter has been specified.
                                                        .executes(ctx -> executeAtEntity(ctx.getSource(), ArgHelpers.getEntity(ctx, "e"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r2"), ArgHelpers.getInt(ctx, "r3"), ArgHelpers.getString(ctx, "filter"), air, false))
                                                )
                                                // r1 applied to x, r2 applied to y and r3 applied to z.
                                                .executes(ctx -> executeAtEntity(ctx.getSource(), ArgHelpers.getEntity(ctx, "e"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r2"), ArgHelpers.getInt(ctx, "r3"), "*", air, false))
                                        )
                                        // r1 applied to x and z, r2 applied to y.
                                        .executes(ctx -> executeAtEntity(ctx.getSource(), ArgHelpers.getEntity(ctx, "e"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r2"), ArgHelpers.getInt(ctx, "r1"), "*", air, false))
                                )
                                // r1 applied to x, y and z.
                                .executes(ctx -> executeAtEntity(ctx.getSource(), ArgHelpers.getEntity(ctx, "e"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r1"), "*", air, false))
                        )
                )
                .then(Commands.argument("x1", BlockPosArgument.blockPos())
                        .then(Commands.argument("y1", BlockPosArgument.blockPos())
                                .then(Commands.argument("z1", BlockPosArgument.blockPos())
                                        .then(Commands.argument("x2", BlockPosArgument.blockPos())
                                                .then(Commands.argument("y2", BlockPosArgument.blockPos())
                                                        .then(Commands.argument("z1", BlockPosArgument.blockPos())
                                                                .then(Commands.argument("filter", StringArgumentType.string())
                                                                        .then(Commands.argument("replacement", StringArgumentType.string())
                                                                                .then(Commands.argument("whole_chunk_mode", BoolArgumentType.bool())
                                                                                        // Full coordinates, filter, replacement block state and chunk mode specified.
                                                                                        .executes(ctx -> execute(ctx.getSource(), ArgHelpers.getBlockPos(ctx, "x1"), ArgHelpers.getBlockPos(ctx, "y1"), ArgHelpers.getBlockPos(ctx, "z1"), ArgHelpers.getBlockPos(ctx, "x2"), ArgHelpers.getBlockPos(ctx, "y2"), ArgHelpers.getBlockPos(ctx, "z2"), ArgHelpers.getString(ctx, "filter"), ArgHelpers.getBlockState(ctx, "replacement"), ArgHelpers.getBool(ctx, "whole_chunk_mode")))
                                                                                )
                                                                                // Full coordinates, filter and replacement block state specified.
                                                                                .executes(ctx -> execute(ctx.getSource(), ArgHelpers.getBlockPos(ctx, "x1"), ArgHelpers.getBlockPos(ctx, "y1"), ArgHelpers.getBlockPos(ctx, "z1"), ArgHelpers.getBlockPos(ctx, "x2"), ArgHelpers.getBlockPos(ctx, "y2"), ArgHelpers.getBlockPos(ctx, "z2"), ArgHelpers.getString(ctx, "filter"), ArgHelpers.getBlockState(ctx, "replacement"), false))
                                                                        )
                                                                        // Full coordinates and filter specified.
                                                                        .executes(ctx -> execute(ctx.getSource(), ArgHelpers.getBlockPos(ctx, "x1"), ArgHelpers.getBlockPos(ctx, "y1"), ArgHelpers.getBlockPos(ctx, "z1"), ArgHelpers.getBlockPos(ctx, "x2"), ArgHelpers.getBlockPos(ctx, "y2"), ArgHelpers.getBlockPos(ctx, "z2"), ArgHelpers.getString(ctx, "filter"), air, false))
                                                                )
                                                                // Full coordinates specified.
                                                                .executes(ctx -> execute(ctx.getSource(), ArgHelpers.getBlockPos(ctx, "x1"), ArgHelpers.getBlockPos(ctx, "y1"), ArgHelpers.getBlockPos(ctx, "z1"), ArgHelpers.getBlockPos(ctx, "x2"), ArgHelpers.getBlockPos(ctx, "y2"), ArgHelpers.getBlockPos(ctx, "z2"), "*", air, false))
                                                        )
                                                )
                                        )
                                )
                        )
                );
    }

    private static int executeAtEntity(CommandSource source, Entity entity, int xRadius, int yRadius, int zRadius, String filter, BlockStateInput replacement, Boolean wholeChunks) {

        BlockPos e = entity.getPosition();

        int x1 = e.getX() - xRadius;
        int y1 = e.getY() - yRadius;
        int z1 = e.getZ() - zRadius;
        int x2 = e.getX() + xRadius;
        int y2 = e.getY() + yRadius;
        int z2 = e.getZ() + zRadius;

        return execute(source, x1, y1, z1, x2, y2, z2, filter, replacement, wholeChunks);
    }

    private static int execute(CommandSource source, BlockPos x1, BlockPos y1, BlockPos z1, BlockPos x2, BlockPos y2, BlockPos z2, String filters, BlockStateInput replacement, Boolean wholeChunks) {

        return execute(source, x1.getX(), y1.getY(), z1.getZ(), x2.getX(), y2.getY(), z2.getZ(), filters, replacement, wholeChunks);
    }

    private static int execute(CommandSource source, int x1, int y1, int z1, int x2, int y2, int z2, String filters, BlockStateInput replacement, Boolean wholeChunks) {

        int rtn = replaceBlocks(source, x1, y1, z1, x2, y2, z2, filters, replacement.getState(), wholeChunks);

        NumberFormat fmt = NumberFormat.getInstance();
        TextComponent component;
        if (rtn == -1) {
            component = new TranslationTextComponent("cofhworld.replaceblocks.failed");
        } else {
            component = FormatHelpers.getTranslationWithFormatting("cofhworld.replaceblocks.successful", fmt.format(totalBlocks), TextFormatting.GOLD, fmt.format(totalReplacedBlocks), TextFormatting.GOLD);
        }

        source.sendFeedback(component, true);

        return rtn;
    }

    private static int replaceBlocks(CommandSource source, int x1, int y1, int z1, int x2, int y2, int z2, String filters, BlockState replacement, boolean wholeChunks) {

        Entity entity = source.getEntity();
        if (entity == null) {
            return 0;
        }

        World world = entity.getEntityWorld();
        if (world.isRemote) {
            return 0;
        }

        source.sendFeedback(new TranslationTextComponent("cofhworld.replaceblocks.lag_warning"), true);

        totalBlocks = 0;
        totalReplacedBlocks = 0;

        ServerWorld sw = (ServerWorld) world;
        BlockFilters blockFilters = new BlockFilters(filters);
        MutableBoundingBox area = CoordinateHelpers.coordinatesToBox(world, x1, y1, z1, x2, y2, z2, wholeChunks);

        for (BlockPos pos : BlockPos.getAllInBoxMutable(area.minX, area.minY, area.minZ, area.maxX, area.maxY, area.maxZ)) {
            BlockState targetState = world.getBlockState(pos).getBlock().getDefaultState();
            if (targetState != replacement && blockFilters.isFilterMatch(targetState)) {
                TileEntity te = sw.getTileEntity(pos);
                if (te != null) {
                    IClearable.clearObj(te);
                }

                // A value of 2 here will send this block change to all clients.
                // A value of 16 will prevent neighbour reactions.
                if (sw.setBlockState(pos, replacement, (2 | 16))) {
                    ++totalReplacedBlocks;
                }
            }

            ++totalBlocks;
        }

        return 1;
    }

}
