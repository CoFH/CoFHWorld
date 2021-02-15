package cofh.cofhworld.command;

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
                                                            // r1 applied to x, r2 applied to y and r3 applied to z. Filter has been specified, as has the replacement block state.
                                                            .executes(ctx -> executeAtEntity(ctx.getSource(), ArgHelpers.getEntity(ctx, "e"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r2"), ArgHelpers.getInt(ctx, "r3"), ArgHelpers.getString(ctx, "filter"), ArgHelpers.getBlockState(ctx, "replacement")))
                                                        )
                                                        // r1 applied to x, r2 applied to y and r3 applied to z. Filter has been specified. No replacement block state specified, air will be used.
                                                        .executes(ctx -> executeAtEntity(ctx.getSource(), ArgHelpers.getEntity(ctx, "e"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r2"), ArgHelpers.getInt(ctx, "r3"), ArgHelpers.getString(ctx, "filter"), air))
                                                )
                                                // r1 applied to x, r2 applied to y and r3 applied to z. No filter specified, a match-all filter is used. No replacement block state specified, air will be used.
                                                .executes(ctx -> executeAtEntity(ctx.getSource(), ArgHelpers.getEntity(ctx, "e"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r2"), ArgHelpers.getInt(ctx, "r3"), "*", air))
                                        )
                                        // r1 applied to x and z, r2 applied to y. No filter specified, a match-all filter is used. No replacement block state specified, air will be used.
                                        .executes(ctx -> executeAtEntity(ctx.getSource(), ArgHelpers.getEntity(ctx, "e"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r2"), ArgHelpers.getInt(ctx, "r1"), "*", air))
                                )
                                // r1 applied to x, y and z. No filter specified, a match-all filter is used. No replacement block state specified, air will be used.
                                .executes(ctx -> executeAtEntity(ctx.getSource(), ArgHelpers.getEntity(ctx, "e"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r1"), ArgHelpers.getInt(ctx, "r1"), "*", air))
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
                                                                            // Full coordinates, filter and replacement block state specified.
                                                                            .executes(ctx -> execute(ctx.getSource(), ArgHelpers.getBlockPos(ctx, "x1"), ArgHelpers.getBlockPos(ctx, "y1"), ArgHelpers.getBlockPos(ctx, "z1"), ArgHelpers.getBlockPos(ctx, "x2"), ArgHelpers.getBlockPos(ctx, "y2"), ArgHelpers.getBlockPos(ctx, "z2"), ArgHelpers.getString(ctx, "filter"), ArgHelpers.getBlockState(ctx, "replacement")))
                                                                        )
                                                                        // Full coordinates and filter specified. No replacement block state specified, air will be used.
                                                                        .executes(ctx -> execute(ctx.getSource(), ArgHelpers.getBlockPos(ctx, "x1"), ArgHelpers.getBlockPos(ctx, "y1"), ArgHelpers.getBlockPos(ctx, "z1"), ArgHelpers.getBlockPos(ctx, "x2"), ArgHelpers.getBlockPos(ctx, "y2"), ArgHelpers.getBlockPos(ctx, "z2"), ArgHelpers.getString(ctx, "filter"), air))
                                                                )
                                                                // Full coordinates specified. No filter specified, match-all filter applied as a default. No replacement block state specified, air will be used.
                                                                .executes(ctx -> execute(ctx.getSource(), ArgHelpers.getBlockPos(ctx, "x1"), ArgHelpers.getBlockPos(ctx, "y1"), ArgHelpers.getBlockPos(ctx, "z1"), ArgHelpers.getBlockPos(ctx, "x2"), ArgHelpers.getBlockPos(ctx, "y2"), ArgHelpers.getBlockPos(ctx, "z2"), "*", air))
                                                        )
                                                )
                                        )
                                )
                        )
                );
    }

    private static int executeAtEntity(CommandSource source, Entity entity, int xRadius, int yRadius, int zRadius, String filter, BlockStateInput replacement) {

        BlockPos p = entity.getPosition();

        int sX = p.getX() - xRadius;
        int sY = p.getY() - yRadius;
        int sZ = p.getZ() - zRadius;
        int eX = p.getX() + xRadius;
        int eY = p.getY() + yRadius;
        int eZ = p.getZ() + zRadius;

        return execute(source, sX, sY, sZ, eX, eY, eZ, filter, replacement);
    }

    private static int execute(CommandSource source, BlockPos sX, BlockPos sY, BlockPos sZ, BlockPos eX, BlockPos eY, BlockPos eZ, String filters, BlockStateInput replacement) {

        return execute(source, sX.getX(), sY.getY(), sZ.getZ(), eX.getX(), eY.getY(), eZ.getZ(), filters, replacement);
    }

    private static int execute(CommandSource source, int sX, int sY, int sZ, int eX, int eY, int eZ, String filters, BlockStateInput replacement) {

        int rtn = replaceBlocks(source, sX, sY, sZ, eX, eY, eZ, filters, replacement.getState());

        NumberFormat fmt = NumberFormat.getInstance();
        TextComponent component;
        if (rtn == -1) {
            component = new TranslationTextComponent("cofhworld.replaceblocks.failed");
        } else {
            component = new TranslationTextComponent("cofhworld.replaceblocks.successful", fmt.format(totalBlocks), fmt.format(totalReplacedBlocks));
        }

        source.sendFeedback(component, true);

        return rtn;
    }

    private static int replaceBlocks(CommandSource source, int sX, int sY, int sZ, int eX, int eY, int eZ, String filters, BlockState replacement) {

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

        source.sendFeedback(new TranslationTextComponent("cofhworld.replaceblocks.lag_warning"), true);

        totalBlocks = 0;
        totalReplacedBlocks = 0;

        ServerWorld sw = (ServerWorld) world;
        BlockFilters blockFilters = new BlockFilters(filters);

        MutableBoundingBox area = MutableBoundingBox.createProper(sX, sY, sZ, eX, eY, eZ);
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
