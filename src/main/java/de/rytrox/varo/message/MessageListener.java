package de.rytrox.varo.message;

import de.rytrox.varo.Varo;
import de.rytrox.varo.moderation.ModeratorManager;
import de.rytrox.varo.scoreboard.Tablist;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MessageListener implements Listener {

    private static final String JOIN_MESSAGE = "%s%s &ahat den Server betreten";
    private static final String QUIT_MESSAGE = "%s%s &chat den Server verlassen";
    private final Varo main;

    public MessageListener(Varo main) {
        this.main = main;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        if(main.getModeratorManager().isModerator(event.getPlayer())) {
            event.setJoinMessage(null);
            return;
        }

        event.setJoinMessage(ChatColor.translateAlternateColorCodes('&',
                String.format(JOIN_MESSAGE,
                Tablist.getInstance().getPrefix(event.getPlayer()),
                event.getPlayer().getName())));

        MessageService.getInstance().writeMessage(event.getJoinMessage(), MessageService.DiscordColor.BLUE);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {

        if(main.getModeratorManager().isModerator(event.getPlayer())) {
            event.setQuitMessage(null);
            return;
        }

        event.setQuitMessage(ChatColor.translateAlternateColorCodes('&',
                String.format(QUIT_MESSAGE,
                        Tablist.getInstance().getPrefix(event.getPlayer()),
                        event.getPlayer().getName())));

        MessageService.getInstance().writeMessage(event.getQuitMessage(), MessageService.DiscordColor.RED);
    }
}
