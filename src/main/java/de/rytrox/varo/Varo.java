package de.rytrox.varo;

import de.rytrox.varo.listener.JoinAndQuitListener;
import de.rytrox.varo.utils.DiscordService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Varo extends JavaPlugin {

    private DiscordService discordService;

    @Override
    public void onEnable() {
        // Plugin startup logic
        discordService = new DiscordService();
        discordService.writeMessage("Der Server wurde gestartet!", DiscordService.DiscordColor.CYAN);

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new JoinAndQuitListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public DiscordService getDiscordWebhook() {
        return discordService;
    }
}
