package YourPluginName.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public abstract class CommandAsset implements CommandExecutor {

    private String commandName, permission;
    private AllowableUserType userType;
    private int argsCount;

    @EventHandler
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (userType == AllowableUserType.CONSOLE && commandSender instanceof Player) {
            return false;
        } else if (userType == AllowableUserType.PLAYER && commandSender instanceof ConsoleCommandSender) {
            return false;
        } else if (strings.length != argsCount) {
            return false;
        }
        return execute(commandSender, strings);
    }

    public abstract boolean execute(CommandSender sender, String[] args);

    public String getPermission() {
        return permission;
    }

    public CommandAsset(String commandName, String permission, AllowableUserType userType, int argsCount) {
        this.commandName = commandName;
        this.permission = permission;
        this.userType = userType;
        this.argsCount = argsCount;
    }

    public String getCommandName() {
        return commandName;
    }

}

