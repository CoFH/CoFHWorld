package cofh.cofhworld.command;

import cofh.cofhworld.CoFHWorld;
import cofh.cofhworld.init.WorldHandler;
import cofh.cofhworld.world.IFeatureGenerator;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.server.command.CommandTreeBase;

import java.util.List;

public class CommandCoFHWorld extends CommandTreeBase {

	@Override
	public String getName() {

		return "cofhworld";
	}

	@Override
	public int getRequiredPermissionLevel() {

		return -1;
	}

	@Override
	public String getUsage(ICommandSender sender) {

		return "cofhworld.usage";
	}

	public CommandCoFHWorld() {

		addSubcommand(new CommandReload());
		addSubcommand(new CommandList());
		addSubcommand(new CommandVersion());
	}

	public static class CommandVersion extends CommandBase {

		@Override
		public String getName() {

			return "version";
		}

		@Override
		public int getRequiredPermissionLevel() {

			return -1;
		}

		@Override
		public String getUsage(ICommandSender sender) {

			return "cofhworld.version.usage";
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

			sender.sendMessage(new TextComponentString(CoFHWorld.VERSION));
		}
	}

	// Command to reload all feature definitions
	public static class CommandReload extends CommandBase {

		@Override
		public String getName() {

			return "reload";
		}

		@Override
		public int getRequiredPermissionLevel() {

			return 4;
		}

		@Override
		public String getUsage(ICommandSender sender) {

			return "cofhworld.reload.usage";
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

			if (WorldHandler.reloadConfig()) {
				notifyCommandListener(sender, this, "cofhworld.reload.successful");
			} else {
				notifyCommandListener(sender, this, "cofhworld.reload.failed");
			}
		}
	}

	// Command to list all feature definitions
	public static class CommandList extends CommandBase {

		@Override
		public String getName() {

			return "list";
		}

		@Override
		public int getRequiredPermissionLevel() {

			return 1;
		}

		@Override
		public String getUsage(ICommandSender sender) {

			return "cofhworld.list.usage";
		}

		@Override
		public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

			final int pageSize = 8;
			List<IFeatureGenerator> generators = WorldHandler.getFeatures();
			int maxPages = (generators.size() - 1) / pageSize;
			int page = args.length == 0 ? 0 : CommandBase.parseInt(args[0], 1, maxPages + 1) - 1;

			TextComponentTranslation component = new TextComponentTranslation("cofhworld.list", page + 1, maxPages + 1);
			component.getStyle().setColor(TextFormatting.GOLD);
			sender.sendMessage(component);

			if (generators.size() == 0) {
				return;
			}

			StringBuilder b = new StringBuilder();
			int maxIndex = Math.min((page + 1) * pageSize, generators.size());
			for (int i = page * pageSize; i < maxIndex; ++i) {
				b.append("* ").append(generators.get(i).getFeatureName()).append('\n');
			}
			b.deleteCharAt(b.length() - 1);

			sender.sendMessage(new TextComponentString(b.toString()));
		}
	}

}
