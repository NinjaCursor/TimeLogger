package TimeSheet.Listeners;

import TimeSheet.Main.TimeSheet;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class LogInListener implements Listener {

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        final long timeStamp = System.currentTimeMillis();

        TimeSheet.getAPI().thenCompose(api -> api.createHandler("LOGINS")).thenAccept((success) -> TimeSheet.getAPI().thenAccept(api -> api.start("LOGINS", uuid.toString(), timeStamp)));
    }

    @EventHandler
    public void onLogOut(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        final long timeStamp = System.currentTimeMillis();

        TimeSheet.getAPI().thenCompose(api -> api.createHandler("LOGINS")).thenAccept((success) -> TimeSheet.getAPI().thenAccept(api -> api.stop("LOGINS", uuid.toString(), timeStamp)));
    }

}
