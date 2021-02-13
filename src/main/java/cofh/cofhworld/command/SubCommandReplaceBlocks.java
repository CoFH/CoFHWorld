package cofh.cofhworld.command;

import cofh.cofhworld.CoFHWorld;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
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

    public static int permissionLevel = 3;
    private static long totalBlocks = 0;
    private static long totalReplacedBlocks = 0;

    private interface PlayerFunction {

        ServerPlayerEntity apply(CommandContext<CommandSource> t) throws CommandSyntaxException;
    }

    public static ArgumentBuilder<CommandSource, ?> registerPlayer() {

        return Commands.literal("replaceblocks")
            .requires(source -> source.hasPermissionLevel(permissionLevel))
            // All default parameters.
            .then(gatherArguments(context -> context.getSource().asPlayer()))
            // Player centred, with radius applied to all directions.
            .then(Commands.argument("p", EntityArgument.player())
                .then(gatherArguments(context -> EntityArgument.getPlayer(context, "p"))));
    }

    public static ArgumentBuilder<CommandSource, ?> registerDirect() {

        return Commands.literal("replaceblocks")
                .requires(source -> source.hasPermissionLevel(permissionLevel))
                .then(Commands.argument("x1", IntegerArgumentType.integer())
                    .then(Commands.argument("y1", IntegerArgumentType.integer())
                        .then(Commands.argument("z1", IntegerArgumentType.integer())
                            .then(Commands.argument("x2", IntegerArgumentType.integer())
                                .then(Commands.argument("y2", IntegerArgumentType.integer())
                                    .then(Commands.argument("z2", IntegerArgumentType.integer())
                                        .then(Commands.argument("filter", StringArgumentType.string())
                                            .then(Commands.argument("replacement", BlockStateArgument.blockState())
                                                .executes(context -> execute(context, IntegerArgumentType.getInteger(context, "x1"), IntegerArgumentType.getInteger(context, "y1"), IntegerArgumentType.getInteger(context, "z1"), IntegerArgumentType.getInteger(context, "x2"), IntegerArgumentType.getInteger(context, "y2"), IntegerArgumentType.getInteger(context, "z2"), StringArgumentType.getString(context, "filter"), BlockStateArgument.getBlockState(context, "replacement")))
                                            ))))))));
    }

    public static ArgumentBuilder<CommandSource, ?> gatherArguments(PlayerFunction playerFunc) {

        BlockStateInput air = new BlockStateInput(Blocks.AIR.getDefaultState(), Collections.emptySet(), null);
        return
            // r1 to be applied to x, y and z.
            Commands.argument("r1", IntegerArgumentType.integer())
                .executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r1"), "*", air))
                    // r1 applied to x and z, r2 applied to y.
                    .then(Commands.argument("r2", IntegerArgumentType.integer())
                        .executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r2"), IntegerArgumentType.getInteger(context, "r1"), "*", air))
                            // r1 applied to x, r2 applied to y and r3 applied to z.
                            .then(Commands.argument("r3", IntegerArgumentType.integer())
                                .executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r2"), IntegerArgumentType.getInteger(context, "r3"), "*", air))
                                    // Radius defined for all sized, plus with type filter.
                                    .then(Commands.argument("filter", StringArgumentType.string())
                                        .executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r2"), IntegerArgumentType.getInteger(context, "r3"), StringArgumentType.getString(context, "filter"), air))
                                            // Radius defined for all directions, plus type filter, plus replacement block ID.
                                            .then(Commands.argument("replacement", BlockStateArgument.blockState())
                                                .executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r2"), IntegerArgumentType.getInteger(context, "r3"), StringArgumentType.getString(context, "filter"), BlockStateArgument.getBlockState(context, "replacement"))
                                            )))));
    }

    private static int executeWithPlayer(CommandContext<CommandSource> context, ServerPlayerEntity player, int xRadius, int yRadius, int zRadius, String filter, BlockStateInput replacement) {

        BlockPos p = player.getPosition();

        int sX = p.getX() - xRadius;
        int sY = p.getY() - yRadius;
        int sZ = p.getZ() - zRadius;
        int eX = p.getX() + xRadius;
        int eY = p.getY() + yRadius;
        int eZ = p.getZ() + zRadius;

        return execute(context, sX, sY, sZ, eX, eY, eZ, filter, replacement);
    }

    private static int execute(CommandContext<CommandSource> context, int sX, int sY, int sZ, int eX, int eY, int eZ, String filters, BlockStateInput replacement) {

        int rtn = replaceBlocks(context, sX, sY, sZ, eX, eY, eZ, filters, replacement.getState());

        NumberFormat fmt = NumberFormat.getInstance();

        TextComponent component;
        if (rtn == -1) {
            component = new TranslationTextComponent("cofhworld.replaceblocks.failed");
        } else {
            component = new TranslationTextComponent("cofhworld.replaceblocks.successful", fmt.format(totalBlocks), fmt.format(totalReplacedBlocks));
        }

        context.getSource().sendFeedback(component, true);

        return rtn;
    }

    private static int replaceBlocks(CommandContext<CommandSource> context, int sX, int sY, int sZ, int eX, int eY, int eZ, String filters, BlockState replacement) {

        Entity entity = context.getSource().getEntity();
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

        context.getSource().sendFeedback(new TranslationTextComponent("cofhworld.replaceblocks.lag_warning"), true);

        ServerWorld sw = (ServerWorld) world;
        BlockFilters blockFilters = new BlockFilters(filters);

        MutableBoundingBox area = MutableBoundingBox.createProper(sX, sY, sZ, eX, eY, eZ);
        for (BlockPos pos : BlockPos.getAllInBoxMutable(area.minX, area.minY, area.minZ, area.maxX, area.maxY, area.maxZ)) {
            CoFHWorld.log.debug(pos.toString());
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
