package cofh.cofhworld.command;

import cofh.cofhworld.feature.IFeatureGenerator;
import cofh.cofhworld.init.WorldHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.command.CommandTreeBase;

public class CommandCoFHWorld extends CommandTreeBase {

    @Override
    public String getName() {
        return "cofhworld";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "cofhworld.usage";
    }

    public CommandCoFHWorld() {
        addSubcommand(new CommandReload());
        addSubcommand(new CommandList());
    }

    // Command to reload all feature definitions
    public static class CommandReload extends CommandBase {
        @Override
        public String getName() {
            return "reload";
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
        public String getUsage(ICommandSender sender) {
            return "cofhworld.list.usage";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            StringBuilder b = new StringBuilder();
            b.append("\n");
//            for (IFeatureGenerator feature: WorldHandler.getFeatures()) {
//                b.append("* " + feature.getFeatureName() + "\n");
//            }
            notifyCommandListener(sender, this, "cofhworld.list", b.toString());
        }
    }
}
