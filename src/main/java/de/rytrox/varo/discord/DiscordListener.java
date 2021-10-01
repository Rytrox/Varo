package de.rytrox.varo.discord;

import de.rytrox.varo.discord.DiscordService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DiscordListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        DiscordService.getInstance().writeMessage(event.getJoinMessage(), DiscordService.DiscordColor.BLUE);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        DiscordService.getInstance().writeMessage(event.getQuitMessage(), DiscordService.DiscordColor.RED);
    }
}
