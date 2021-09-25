package de.rytrox.varo.listener;

import de.rytrox.varo.Varo;
import de.rytrox.varo.utils.DiscordService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class JoinAndQuitListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        JavaPlugin.getPlugin(Varo.class).getDiscordService().writeMessage(event.getJoinMessage(), DiscordService.DiscordColor.BLUE);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        JavaPlugin.getPlugin(Varo.class).getDiscordService().writeMessage(event.getQuitMessage(), DiscordService.DiscordColor.RED);
    }
}
