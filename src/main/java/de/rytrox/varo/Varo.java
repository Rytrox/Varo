package de.rytrox.varo;

import de.rytrox.varo.commands.CMDgamestate;
import de.rytrox.varo.listener.JoinAndQuitListener;
import de.rytrox.varo.listener.PlayerDeathListener;
import de.rytrox.varo.utils.DiscordService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Varo extends JavaPlugin {

    @Override
    public void onEnable() {

        DiscordService.getInstance().writeMessage("Der Server wurde gestartet!", DiscordService.DiscordColor.CYAN);

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new JoinAndQuitListener(), this);
        pluginManager.registerEvents(new PlayerDeathListener(), this);

        this.getCommand("gamestate").setExecutor(new CMDgamestate());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
