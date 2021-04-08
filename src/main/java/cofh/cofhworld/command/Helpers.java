package cofh.cofhworld.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.function.Predicate;

public class Helpers {

	public static class FilteredAreaArgumentHelpers {

		public static final String ENTITY = "entity",
				RADIUS_X = "radius", RADIUS_Y = "radius-y", RADIUS_Z = "radius-z",
				START = "start", END = "end",
				FILTER = "filter",
				WHOLE_CHUNK = "whole-chunk";

		public interface PredicateProvider<T> {

			Predicate<T> getFilter(CommandContext<CommandSource> t) throws CommandSyntaxException;
		}

		public interface BooleanProvider {

			boolean getBoolean(CommandContext<CommandSource> context);
		}

		public interface PredicateProviderConsumer {

			Command<CommandSource> withFilter(PredicateProvider<CachedBlockInfo> getter, BooleanProvider wholeChunk);
		}

		public interface ExecutableFilteredAreaCommand {

			int execute(CommandContext<CommandSource> context, BlockPos start, BlockPos end, Predicate<CachedBlockInfo> filter, boolean wholeChunks);
		}

		public static ArgumentBuilder<CommandSource, ?> gatherArguments(ArgumentBuilder<CommandSource, ?> baseCmd, ExecutableFilteredAreaCommand cmd) {

			return baseCmd.then(
					Commands.argument(ENTITY, EntityArgument.entity()).
							then(
									withWithoutFilter(Commands.argument(RADIUS_X, IntegerArgumentType.integer()).then(
											withWithoutFilter(Commands.argument(RADIUS_Y, IntegerArgumentType.integer()).then(
													withWithoutFilter(Commands.argument(RADIUS_Z, IntegerArgumentType.integer())
															, (filter, chunk) -> context ->
																	// [entity] [radius-x] [radius-y] [radius-z] [filter]? [chunk]?
																	executeAtEntity(context, cmd, EntityArgument.getEntity(context, ENTITY),
																			IntegerArgumentType.getInteger(context, RADIUS_X),
																			IntegerArgumentType.getInteger(context, RADIUS_Y),
																			IntegerArgumentType.getInteger(context, RADIUS_Z),
																			filter.getFilter(context),
																			chunk.getBoolean(context))))
													, (filter, chunk) -> context ->
															// [entity] [radius-xz] [radius-y] [filter]? [chunk]?
															executeAtEntity(context, cmd, EntityArgument.getEntity(context, ENTITY),
																	IntegerArgumentType.getInteger(context, RADIUS_X),
																	IntegerArgumentType.getInteger(context, RADIUS_Y),
																	IntegerArgumentType.getInteger(context, RADIUS_X),
																	filter.getFilter(context),
																	chunk.getBoolean(context))))
											, (filter, chunk) -> context ->
													// [entity] [radius] [filter]? [chunk]?
													executeAtEntity(context, cmd, EntityArgument.getEntity(context, ENTITY),
															IntegerArgumentType.getInteger(context, RADIUS_X),
															IntegerArgumentType.getInteger(context, RADIUS_X),
															IntegerArgumentType.getInteger(context, RADIUS_X),
															filter.getFilter(context),
															chunk.getBoolean(context)))
							)
			).then(
					Commands.argument(START, BlockPosArgument.blockPos()).
							then(
									Commands.argument(END, BlockPosArgument.blockPos()).
											then(
													Commands.argument(FILTER, BlockPredicateListArgument.blockPredicateList()).
															then(
																	Commands.argument(WHOLE_CHUNK, BoolArgumentType.bool()).
																			// [sX sY sZ] [eX eY eZ] [filter] [filter?]... [whole_chunk]
																					executes(ctx -> cmd.execute(ctx,
																					BlockPosArgument.getBlockPos(ctx, START),
																					BlockPosArgument.getBlockPos(ctx, END),
																					BlockPredicateListArgument.getBlockPredicateList(ctx, FILTER),
																					BoolArgumentType.getBool(ctx, WHOLE_CHUNK)))
															).
															// [sX sY sZ] [eX eY eZ] [filter] [filter?]...
																	executes(ctx -> cmd.execute(ctx,
																	BlockPosArgument.getBlockPos(ctx, START),
																	BlockPosArgument.getBlockPos(ctx, END),
																	BlockPredicateListArgument.getBlockPredicateList(ctx, FILTER),
																	false))
											)
							)
			);
		}

		public static ArgumentBuilder<CommandSource, ?> withWithoutFilter(ArgumentBuilder<CommandSource, ?> argument, PredicateProviderConsumer command) {

			return argument.
//					executes(command.withFilter(p -> v -> true, p -> false)).
//					then(
//							Commands.argument(WHOLE_CHUNK, BoolArgumentType.bool()).
//									executes(command.withFilter(context -> v -> true, ctx -> BoolArgumentType.getBool(ctx, WHOLE_CHUNK)))
//					).
					then(
							Commands.argument(FILTER, BlockPredicateListArgument.blockPredicateList()).
									executes(command.withFilter(context -> BlockPredicateListArgument.getBlockPredicateList(context, FILTER), p -> false)).
									then(
											Commands.argument(WHOLE_CHUNK, BoolArgumentType.bool()).
													executes(command.withFilter(context -> BlockPredicateListArgument.getBlockPredicateList(context, FILTER),
															ctx -> BoolArgumentType.getBool(ctx, WHOLE_CHUNK)))
									)
					);
		}

		private static int executeAtEntity(CommandContext<CommandSource> context, ExecutableFilteredAreaCommand cmd,
				Entity entity, int xRadius, int yRadius, int zRadius, Predicate<CachedBlockInfo> filters, boolean wholeChunks) {

			BlockPos p = entity.getPosition();

			int x1 = p.getX() - xRadius;
			int y1 = p.getY() - yRadius;
			int z1 = p.getZ() - zRadius;
			int x2 = p.getX() + xRadius;
			int y2 = p.getY() + yRadius;
			int z2 = p.getZ() + zRadius;

			// TODO - in 1.17 this should be modified to use the getBottomY and getTopY methods of the HeightLimitView interface.
			// The assumption that y == 0 is the lower bound of the world is not valid in 1.17.
			final int MIN_Y = 0, MAX_Y = context.getSource().getWorld().getHeight();

			// Clamp y values to the dimension world height build limits.
			// Do not use 256 here as it is variable in 1.17 and above.
			if (y1 < MIN_Y) {
				y1 = MIN_Y;
			} else if (y1 > MAX_Y) {
				y1 = MAX_Y;
			}

			if (y2 < MIN_Y) {
				y2 = MIN_Y;
			} else if (y2 > MAX_Y) {
				y2 = MAX_Y;
			}

			return cmd.execute(context, new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2), filters, wholeChunks);
		}
	}

	public static class CoordinateHelpers {

		public static MutableBoundingBox coordinatesToBox(World world, BlockPos start, BlockPos end, boolean wholeChunks) {

			int x1 = start.getX();
			int y1 = start.getY();
			int z1 = start.getZ();

			int x2 = end.getX();
			int y2 = end.getY();
			int z2 = end.getZ();

			// TODO - in 1.17 this should be modified to use the getBottomY and getTopY methods of the HeightLimitView interface.
			// The assumption that y == 0 is the lower bound of the world is not valid in 1.17.
			final int MIN_Y = 0, MAX_Y = world.getHeight();

			// Clamp y values to the dimension world height build limits.
			// Do not use 256 here as it is variable in 1.17 and above.
			if (y1 < MIN_Y) {
				y1 = MIN_Y;
			} else if (y1 > MAX_Y) {
				y1 = MAX_Y;
			}

			if (y2 < MIN_Y) {
				y2 = MIN_Y;
			} else if (y2 > MAX_Y) {
				y2 = MAX_Y;
			}

			// This will select a minimal sized cuboid that will contain all of the chunks which the coordinate range fit within.
			if (wholeChunks) {
				ChunkPos cp1 = world.getChunkAt(new BlockPos(x1, y1, z1)).getPos();
				x1 = cp1.getXStart();
				y1 = 0;
				z1 = cp1.getZStart();

				ChunkPos cp2 = world.getChunkAt(new BlockPos(x2, y2, z2)).getPos();
				x2 = cp2.getXEnd();
				y2 = MAX_Y;
				z2 = cp2.getZEnd();
			}

			return MutableBoundingBox.createProper(x1, y1, z1, x2, y2, z2);
		}

	}

	public static class FormatHelpers {

		public static IFormattableTextComponent getStringWithFormatting(String value, TextFormatting format) {

			return new StringTextComponent(value).mergeStyle(format);
		}

		public static IFormattableTextComponent getTranslationWithFormatting(String value, TextFormatting format) {

			return new TranslationTextComponent(value).mergeStyle(format);
		}

		public static TranslationTextComponent getTranslationWithFormatting(String key, IFormattableTextComponent... args) {

			return new TranslationTextComponent(key, (Object[]) args);
		}

		public static TranslationTextComponent getTranslationWithFormatting(String key, Iterable<Pair<String, TextFormatting>> args) {

			ArrayList<IFormattableTextComponent> components = new ArrayList<>();

			for (Pair<String, TextFormatting> arg : args) {
				components.add(getStringWithFormatting(arg.getLeft(), arg.getRight()));
			}

			return new TranslationTextComponent(key, components.toArray());
		}

	}

}
