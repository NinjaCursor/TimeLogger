package YourPluginName.Listeners;

import YourPluginName.Main.Main;
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
        Main.getTimeManager().start(uuid);
        Main.log().log("Login detected!");
    }

    @EventHandler
    public void onLogOut(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Main.getTimeManager().stop(uuid);
        Main.log().log("Logout detected!");
    }

}
