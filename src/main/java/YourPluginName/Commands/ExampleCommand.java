package YourPluginName.Commands;

import YourPluginName.Main.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ExampleCommand extends CommandAsset {
    public ExampleCommand(String commandName, String permission) {
        super(commandName, permission, AllowableUserType.ANY, 0);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        //todo check for when time manager is not loaded
        sender.sendMessage(String.format("Your time is %s", Main.getTimeManager().getTimePlayer(player.getUniqueId()).getAccData().getTotal()));
        sender.sendMessage(String.format("Your log consists of %s many logs", Main.getTimeManager().getTimePlayer(player.getUniqueId()).getLogData().size()));
        return true;
    }
}
