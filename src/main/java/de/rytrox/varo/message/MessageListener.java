package de.rytrox.varo.message;

import de.rytrox.varo.Varo;
import de.rytrox.varo.teams.scoreboard.Tablist;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class MessageListener implements Listener {

    private static final String JOIN_MESSAGE = "%s%s &ahat den Server betreten";
    private static final String QUIT_MESSAGE = "%s%s &chat den Server verlassen";

    private final Varo main;

    public MessageListener(@NotNull Varo main) {
        this.main = main;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        if(!main.getModeratorManager().isModerator(event.getPlayer())) {
            main.getMessageService()
                    .writeMessage(ChatColor.translateAlternateColorCodes('&',
                                    String.format(JOIN_MESSAGE,
                                            Tablist.getInstance().getPrefix(event.getPlayer()),
                                            event.getPlayer().getName())),
                            MessageService.DiscordColor.RED
                    );
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        if(!main.getModeratorManager().isModerator(event.getPlayer())) {
            main.getMessageService()
                    .writeMessage(ChatColor.translateAlternateColorCodes('&',
                        String.format(QUIT_MESSAGE,
                                Tablist.getInstance().getPrefix(event.getPlayer()),
                                event.getPlayer().getName())),
                        MessageService.DiscordColor.RED
                    );
        }
    }
}
