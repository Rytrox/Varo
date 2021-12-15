package de.rytrox.varo.discord;

import de.rytrox.varo.Varo;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DiscordListener implements Listener {

    private final Varo main;
    public DiscordListener(Varo main) {
        this.main = main;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        MessageService.getInstance(main)
                .writeMessage(event.getJoinMessage(), MessageService.DiscordColor.BLUE);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        MessageService.getInstance(main)
                .writeMessage(event.getQuitMessage(), MessageService.DiscordColor.RED);
    }
}
