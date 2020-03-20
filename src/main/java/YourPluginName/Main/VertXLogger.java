package YourPluginName.Main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class VertXLogger {

    private String prefix;
    public VertXLogger(String name) {
        this.prefix = "[ " + name + " ]: ";
    }

    public void log(String message) {
        Bukkit.getLogger().info(this.prefix + message);
    }

    public void error(String message) {
        log(ChatColor.RED + message);
    }

    public void warning(String message) {
        log(ChatColor.YELLOW + message);
    }

}
