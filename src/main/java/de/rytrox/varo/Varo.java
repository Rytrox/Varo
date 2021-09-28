package de.rytrox.varo;

import de.rytrox.varo.commands.CMDgamestate;
import de.rytrox.varo.listener.JoinAndQuitListener;
import de.rytrox.varo.listener.PlayerDeathListener;
import de.rytrox.varo.utils.DiscordService;
import de.rytrox.varo.utils.GameStateHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Varo extends JavaPlugin {

    private GameStateHandler gameStateHandler;
    private DiscordService discordService;

    @Override
    public void onEnable() {

        this.gameStateHandler = new GameStateHandler();

        this.discordService = new DiscordService();
        this.discordService.writeMessage("Der Server wurde gestartet!", DiscordService.DiscordColor.CYAN);

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new JoinAndQuitListener(), this);
        pluginManager.registerEvents(new PlayerDeathListener(), this);

        this.getCommand("gamestate").setExecutor(new CMDgamestate());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public GameStateHandler getGameStateHandler() {
        return gameStateHandler;
    }

    public DiscordService getDiscordService() {
        return discordService;
    }
}
