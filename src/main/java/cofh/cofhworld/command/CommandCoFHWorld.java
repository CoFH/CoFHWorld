package cofh.cofhworld.command;

import cofh.cofhworld.CoFHWorld;
import cofh.cofhworld.init.WorldHandler;
import cofh.cofhworld.world.IFeatureGenerator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModList;

import java.util.List;
import java.util.function.Function;

public class CommandCoFHWorld {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {

		dispatcher.register(
				LiteralArgumentBuilder.<CommandSource>literal("cofhworld")
						.then(SubCommandVersion.register())
						.then(SubCommandList.register())
						.then(SubCommandReload.register())
						.then(SubCommandCountBlocks.register())
		);
	}

	private static class SubCommandVersion {

		public static ArgumentBuilder<CommandSource, ?> register() {

			return Commands.literal("version")
					.executes(SubCommandVersion::execute);
		}

		public static int execute(CommandContext<CommandSource> context) throws CommandException {

			context.getSource().sendFeedback(new StringTextComponent(
					String.valueOf(ModList.get().getModFileById(CoFHWorld.MOD_ID).getFile().getSubstitutionMap().get().getOrDefault("jarVersion", "DEV"))
			), true);
			return 1;
		}
	}

	// Command to reload all feature definitions
	private static class SubCommandReload {

		public static ArgumentBuilder<CommandSource, ?> register() {

			return Commands.literal("reload")
					.requires(source -> source.hasPermissionLevel(4))
					.executes(SubCommandReload::execute);
		}

		public static int execute(CommandContext<CommandSource> context) throws CommandException {

			String key;
			int rtn = 0;
			if (WorldHandler.reloadConfig()) {
				key = "cofhworld.reload.successful";
				rtn = 1;
			} else {
				key = "cofhworld.reload.failed";
			}

			context.getSource().sendFeedback(new TranslationTextComponent(key), true);
			return rtn;
		}
	}

	private static class SubCommandCountBlocks {

		public static int permissionLevel = 3;

		/*public static ArgumentBuilder<CommandSource, ?> register() {
			return Commands.literal("countblocks")
					.requires(source -> source.hasPermissionLevel(permissionLevel))
					// All default parameters.
					.executes(context -> executeWithPlayer(context, context.getSource().asPlayer(), 32, 32, 32, "*"))
					// Player centred, with radius applied to all directions.
					.then(Commands.argument("p", EntityArgument.player())
						.executes(context -> executeWithPlayer(context, EntityArgument.getPlayer(context, "p"), 32, 32, 32, "*")))
					// Player centred, with radius applied to all directions.
					.then(Commands.argument("r1", IntegerArgumentType.integer())
						.executes(context -> executeWithPlayer(context, EntityArgument.getPlayer(context, "p"), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r1"), "*")))
					// Player centred, with radius r1 applied to x and z, r2 applied to y.
					.then(Commands.argument("r2", IntegerArgumentType.integer())
						.executes(context -> executeWithPlayer(context, EntityArgument.getPlayer(context, "p"), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r2"), IntegerArgumentType.getInteger(context, "r1"), "*")))
					// Player centred, with radius r1 applied to x, r2 applied to y and r3 applied to z.
					.then(Commands.argument("r3", IntegerArgumentType.integer())
						.executes(context -> executeWithPlayer(context, EntityArgument.getPlayer(context, "p"), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r2"), IntegerArgumentType.getInteger(context, "r3"), "*")))
					// Player centred with full radius and with type filter.
					.then(Commands.argument("filter", StringArgumentType.string())
						.executes(context -> executeWithPlayer(context, EntityArgument.getPlayer(context, "p"), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r2"), IntegerArgumentType.getInteger(context, "r3"), StringArgumentType.getString(context, "filter"))));

		}*/

		public static ArgumentBuilder<CommandSource, ?> register() {
			return Commands.literal("countblocks")
				.requires(source -> source.hasPermissionLevel(permissionLevel))
				// All default parameters.
				.then(gatherArguments(context -> context.getSource().asPlayer())
					// Player centred, with radius applied to all directions.
					.then(Commands.argument("p", EntityArgument.player()))
					.then(gatherArguments(context -> EntityArgument.getPlayer(context, "p")))
				);
		}

		public static ArgumentBuilder<CommandSource, ?> gatherArguments(Function<CommandContext<CommandSource>, ServerPlayerEntity> playerFunc) {
			return
					// Player centred, with radius applied to all directions.
					Commands.argument("r1", IntegerArgumentType.integer())
							.executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r1"), "*"))
							// Player centred, with radius r1 applied to x and z, r2 applied to y.
							.then(Commands.argument("r2", IntegerArgumentType.integer())
									.executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r2"), IntegerArgumentType.getInteger(context, "r1"), "*"))
									// Player centred, with radius r1 applied to x, r2 applied to y and r3 applied to z.
									.then(Commands.argument("r3", IntegerArgumentType.integer())
											.executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r2"), IntegerArgumentType.getInteger(context, "r3"), "*"))
											// Player centred with full radius and with type filter.
											.then(Commands.argument("filter", StringArgumentType.string())
													.executes(context -> executeWithPlayer(context, playerFunc.apply(context), IntegerArgumentType.getInteger(context, "r1"), IntegerArgumentType.getInteger(context, "r2"), IntegerArgumentType.getInteger(context, "r3"), StringArgumentType.getString(context, "filter"))))));

		}

		public static int executeWithPlayer(CommandContext<CommandSource> context, ServerPlayerEntity player, int xRadius, int yRadius, int zRadius, String filter) {
			BlockPos p = player.getPosition();

			int sX = p.getX() - xRadius;
			int sY = p.getY() - yRadius;
			int sZ = p.getZ() - zRadius;
			int eX = p.getX() + xRadius;
			int eY = p.getY() + yRadius;
			int eZ = p.getZ() + zRadius;

			return execute(context, sX, sY, sZ, eX, eY, eZ, filter);
		}

		public static int execute(CommandContext<CommandSource> context, int sX, int sY, int sZ, int eX, int eY, int eZ, String filter) throws CommandException {

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

			StringBuilder s = new StringBuilder()
				.append("start: (")
				.append(sX)
				.append(",")
				.append(sY)
				.append(",")
				.append(sZ)
				.append(") ")
				.append("end: (")
				.append(eX)
				.append(",")
				.append(eY)
				.append(",")
				.append(eZ)
				.append(")");

			context.getSource().sendFeedback(new TranslationTextComponent(s.toString()), true);

			String key;
			int rtn = 0;
			if (true) {
				key = "cofhworld.countblocks.successful";
				rtn = 1;
			} else {
				key = "cofhworld.countblocks.failed";
			}

			context.getSource().sendFeedback(new TranslationTextComponent(key), true);
			return rtn;
		}
	}

	// Command to list all feature definitions
	public static class SubCommandList {

		final private static int PAGE_SIZE = 8;

		public static ArgumentBuilder<CommandSource, ?> register() {

			return Commands.literal("list")
					.requires(source -> source.hasPermissionLevel(1))
					.executes(SubCommandList::execute)
					.then(Commands.argument("page", IntegerArgumentType.integer(1, Math.max(1, (WorldHandler.getFeatures().size() - 1) / PAGE_SIZE) + 1))
							.executes(SubCommandList::executeWithPage));
		}

		public static int execute(CommandContext<CommandSource> context) throws CommandException {

			return execute(context, 0);
		}

		public static int executeWithPage(CommandContext<CommandSource> context) throws CommandException {

			return execute(context, IntegerArgumentType.getInteger(context, "page"));
		}

		public static int execute(CommandContext<CommandSource> context, int page) throws CommandException {

			List<IFeatureGenerator> generators = WorldHandler.getFeatures();
			int maxPages = (generators.size() - 1) / PAGE_SIZE;

			TextComponent component = new TranslationTextComponent("cofhworld.list", page + 1, maxPages + 1);
			component.getStyle().setColor(Color.fromTextFormatting(TextFormatting.GOLD));
			context.getSource().sendFeedback(component, true);

			if (generators.size() == 0) {
				component = new StringTextComponent("! EMPTY");
				component.getStyle().setColor(Color.fromTextFormatting(TextFormatting.DARK_RED)); // TODO: PROBABLY BROKEN
				context.getSource().sendFeedback(component, true);
				return 1;
			}

			StringBuilder b = new StringBuilder();
			int maxIndex = Math.min((page + 1) * PAGE_SIZE, generators.size());
			for (int i = page * PAGE_SIZE; i < maxIndex; ++i) {
				b.append("* ").append(generators.get(i).getFeatureName()).append('\n');
			}
			b.deleteCharAt(b.length() - 1);

			component = new StringTextComponent(b.toString());
			component.getStyle().setColor(Color.fromTextFormatting(TextFormatting.DARK_BLUE));
			context.getSource().sendFeedback(component, true);
			return 1;
		}
	}

}
