package TimeSheet.Commands;
import TimeSheet.Main.TimeSheet;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.Callable;

public class TotalTimeCommand extends CommandAsset {
    public TotalTimeCommand(String commandName, String permission) {
        super(commandName, permission, AllowableUserType.PLAYER, 0);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        TimeSheet.getAPI().thenCompose(api -> api.getTimePackage(player.getUniqueId().toString(), "LOGINS")).thenAccept((data) -> {
            Bukkit.getScheduler().callSyncMethod(TimeSheet.getPlugin(), new Callable<Long>() {
                @Override
                public Long call() throws Exception {
                    if (data == null) {
                        player.sendMessage("An error occurred loading your time.");
                        return 0L;
                    }

                    long seconds = data.getSummaryData().getTotal() / 1000;

                    player.sendMessage(String.format("%dh:%02dm", seconds / 3600, (seconds % 3600)/ 60));
                    return 1L;
                }
            });
        });
        return true;
    }
}
