package de.rytrox.varo.discord;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DiscordListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        MessageService.getInstance().writeMessage(event.getJoinMessage(), MessageService.DiscordColor.BLUE);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        MessageService.getInstance().writeMessage(event.getQuitMessage(), MessageService.DiscordColor.RED);
    }
}
