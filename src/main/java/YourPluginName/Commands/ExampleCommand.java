package YourPluginName.Commands;

import org.bukkit.command.CommandSender;

public class ExampleCommand extends CommandAsset {
    public ExampleCommand(String commandName, String permission) {
        super(commandName, permission, AllowableUserType.ANY, 0);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("The command worked!");
        return true;
    }
}
