package cofh.cofhworld.command;

import cofh.cofhworld.CoFHWorld;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;

import java.util.*;

public class SubCommandCountBlocks {

    public static int permissionLevel = 3;

    private static long totalBlocks = 0;
    private static long totalMatchedBlocks = 0;

    private interface PlayerFunction {

        ServerPlayerEntity apply(CommandContext<CommandSource> t) throws CommandSyntaxException;
    }

    public static ArgumentBuilder<CommandSource, ?> register() {

        return Commands.literal("countblocks")
                .requires(source -> source.hasPermissionLevel(permissionLevel))
                // All default parameters.
                .then(gatherArguments(context -> context.getSource().asPlayer()))
                // Player centred, with radius applied to all directions.
                .then(Commands.argument("p", EntityArgument.player())
                        .then(gatherArguments(context -> EntityArgument.getPlayer(context, "p"))));
    }

    public static ArgumentBuilder<CommandSource, ?> gatherArguments(PlayerFunction playerFunc) {

        return
                // r1 to be applied to x, y and z.
                Commands.argument("r1", IntegerArgumentType.integer())
                        .executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r1"), "*"))
                        // r1 applied to x and z, r2 applied to y.
                        .then(Commands.argument("r2", IntegerArgumentType.integer())
                                .executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r2"), IntegerArgumentType.getInteger(context, "r1"), "*"))
                                // r1 applied to x, r2 applied to y and r3 applied to z.
                                .then(Commands.argument("r3", IntegerArgumentType.integer())
                                        .executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r2"), IntegerArgumentType.getInteger(context, "r3"), "*"))
                                        // Radius defined for all directions, plus type filter
                                        .then(Commands.argument("filter", StringArgumentType.string())
                                                .executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r2"), IntegerArgumentType.getInteger(context, "r3"), StringArgumentType.getString(context, "filter")))
                                        )));

    }

    private static int executeWithPlayer(CommandContext<CommandSource> context, ServerPlayerEntity player, int xRadius, int yRadius, int zRadius, String filter) {

        BlockPos p = player.getPosition();

        int sX = p.getX() - xRadius;
        int sY = p.getY() - yRadius;
        int sZ = p.getZ() - zRadius;
        int eX = p.getX() + xRadius;
        int eY = p.getY() + yRadius;
        int eZ = p.getZ() + zRadius;

        return execute(context, sX, sY, sZ, eX, eY, eZ, filter);
    }

    private static int execute(CommandContext<CommandSource> context, int sX, int sY, int sZ, int eX, int eY, int eZ, String filters) {

        int rtn = countBlocks(context, sX, sY, sZ, eX, eY, eZ, filters);

        TextComponent component;

        if (rtn == -1) {
            component = new TranslationTextComponent("cofhworld.countblocks.failed");
            component.getStyle().setColor(Color.fromTextFormatting(TextFormatting.RED));
        } else {
            component = new TranslationTextComponent("cofhworld.countblocks.successful", totalBlocks, totalMatchedBlocks);
            component.getStyle().setColor(Color.fromTextFormatting(TextFormatting.GOLD));
        }

        context.getSource().sendFeedback(component, true);

        return rtn;
    }

    private static int countBlocks(CommandContext<CommandSource> context, int sX, int sY, int sZ, int eX, int eY, int eZ, String filters) {

        Entity entity = context.getSource().getEntity();
        if (entity == null) {
            return 0;
        }

        World world = entity.getEntityWorld();
        if (world.isRemote) {
            return 0;
        }

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

        //String dbg = ""
        //        + "start: (" + sX + "," + sY + "," + sZ + ") "
        //        + "end: (" + eX + "," + eY + "," + eZ + ")";
        //CoFHWorld.log.debug(dbg);

        BlockFilters blockFilters = new BlockFilters(filters);
        HashMap<BlockState, Integer> blockCounts = new HashMap<>();

        totalBlocks = 0;
        totalMatchedBlocks = 0;

        long start = System.nanoTime();

        for (int x = sX; x <= eX; x++) {
            for (int z = sZ; z <= eZ; z++) {
                for (int y = sY; y <= eY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState defaultState = world.getBlockState(pos).getBlock().getDefaultState();
                    if (blockFilters.isFilterMatch(defaultState)) {
                        int ofTypeCount = 1;
                        if (blockCounts.containsKey(defaultState)) {
                            ofTypeCount = blockCounts.get(defaultState) + 1;
                        }

                        blockCounts.put(defaultState, ofTypeCount);
                        ++totalMatchedBlocks;
                    }

                    ++totalBlocks;
                }
            }
        }

        long end = System.nanoTime();
        long exec = end - start;
        double inSeconds = (double)exec / 1_000_000_000.0;

        CoFHWorld.log.debug("The program takes "+exec+" nanoseconds that is "+inSeconds+" seconds to execute.");

        for (Map.Entry<BlockState, Integer> entry : blockCounts.entrySet()) {
            Block b = entry.getKey().getBlock();
            int count = entry.getValue();

            CoFHWorld.log.debug(b.toString() + " " + count);

            //totalMatchingBlocks += count;
        }

        CoFHWorld.log.debug("Total blocks scanned: " + totalBlocks);
        CoFHWorld.log.debug("Total blocks matched: " + totalMatchedBlocks);

        return 1;
    }
}
