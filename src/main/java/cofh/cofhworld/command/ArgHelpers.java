package cofh.cofhworld.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class ArgHelpers {

    public static Entity getEntity(CommandContext<CommandSource> context, String name) throws CommandSyntaxException {

        return EntityArgument.getEntity(context , name);
    }

    public static int getInt(CommandContext<CommandSource> context, String name) {

        return IntegerArgumentType.getInteger(context, name);
    }

    public static String getString(CommandContext<CommandSource> context , String name) {

        return StringArgumentType.getString(context, name);
    }

    public static BlockPos getBlockPos(CommandContext<CommandSource> context, String name) throws CommandSyntaxException {

        return BlockPosArgument.getBlockPos(context, name);
    }

    public static BlockStateInput getBlockState(CommandContext<CommandSource> context, String name) {

        return BlockStateArgument.getBlockState(context, name);
    }
}
