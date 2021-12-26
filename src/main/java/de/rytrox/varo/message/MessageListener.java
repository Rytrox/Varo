package de.rytrox.varo.message;

import de.rytrox.varo.Varo;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MessageListener implements Listener {

    private final Varo main;

    public MessageListener(Varo main) {
        this.main = main;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        MessageService.getInstance().writeMessage(event.getJoinMessage(), MessageService.DiscordColor.BLUE);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        MessageService.getInstance().writeMessage(event.getQuitMessage(), MessageService.DiscordColor.RED);
    }
}
