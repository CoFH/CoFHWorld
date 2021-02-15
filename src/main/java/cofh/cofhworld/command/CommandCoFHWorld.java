package cofh.cofhworld.command;

import cofh.cofhworld.CoFHWorld;
import cofh.cofhworld.init.WorldHandler;
import cofh.cofhworld.world.IFeatureGenerator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.*;
import net.minecraftforge.fml.ModList;

import java.util.*;

public class CommandCoFHWorld {

	public static void register(CommandDispatcher<CommandSource> dispatcher) {

		dispatcher.register(
				LiteralArgumentBuilder.<CommandSource>literal("cofhworld")
						.then(SubCommandVersion.register())
						.then(SubCommandList.register())
						.then(SubCommandReload.register())
						.then(SubCommandCountBlocks.register())
						.then(SubCommandCountBlocks.registerPageList())
						.then(SubCommandReplaceBlocks.register())
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
