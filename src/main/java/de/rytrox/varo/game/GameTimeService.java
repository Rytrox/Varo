package de.rytrox.varo.game;

import de.rytrox.varo.Varo;
import de.rytrox.varo.game.events.GameDayEndEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Service that manages the GameTime based on the internal System-Time
 *
 * @author Timeout
 */
public class GameTimeService implements Listener {

    private final Varo main;

    private final LocalTime startTime;
    private final LocalTime endTime;

    private final String kickMessage;

    public GameTimeService(@NotNull Varo main) {
        this.main = main;
        Bukkit.getPluginManager().registerEvents(new PlayerTimeService(main), main);

        this.startTime = LocalTime.parse(main.getConfig().getString("game.start", "16:00"));
        this.endTime = LocalTime.parse(main.getConfig().getString("game.end", "22:00"));

        kickMessage = ChatColor.translateAlternateColorCodes('&',
                "&8[&6Varo&8] &cDer aktuelle Spieltag ist zu Ende!\n" +
                        String.format("&cDas Spiel wird morgen um %s fortgesetzt", startTime.format(DateTimeFormatter.ofPattern("HH:mm"))));

        registerEndScheduler();
    }

    /**
     * Creates and starts the End-Scheduler.
     * This scheduler kicks all players from the server that are online at the end-time
     */
     public void registerEndScheduler() {
        Bukkit.getScheduler().runTaskLater(main, () ->
            Bukkit.getScheduler().runTaskTimer(main, () -> {
                        Bukkit.getOnlinePlayers().forEach(player ->
                                player.kickPlayer(this.kickMessage)
                        );

                        Bukkit.getPluginManager().callEvent(new GameDayEndEvent());
                        },
                    0, 24 * 60 * 60 * 20)
        , getTimerOffset());
    }

    /**
     * Calculates the Offset between the current time and the end time in Ticks
     *
     * @return the amount of ticks between the current time and the end time of the day
     */
    public long getTimerOffset() {
        LocalTime offset;

        // Don't use LocalTime#between or LocalTime#until here! Wrong behavior
        if(endTime.isBefore(LocalTime.now())) {
            // First calculate the distance between 00:00 and the current time <- Offset of last day
            offset = LocalTime.MIDNIGHT.minusHours(LocalTime.now().getHour())
                                       .minusMinutes(LocalTime.now().getMinute());
            // Then add the offset of the last day to the current end date.
            // This is mathematically smaller than 1 day in total!
            offset = endTime.plusHours(offset.getHour())
                            .plusMinutes(offset.getMinute());
        } else {
            // Calculate delta time simple
            offset = endTime.minusHours(LocalTime.now().getHour())
                            .minusMinutes(LocalTime.now().getMinute());
        }

        // calculate offset in ticks (minutes) * 60 * 20
        return (offset.getHour() * 60 + offset.getMinute()) * 60 * 20L;
    }

    @EventHandler
    public void onInvalidJoin(PlayerLoginEvent event) {
        // Do not allow Joins out of the allowed timespan
        if(LocalTime.now().isBefore(startTime) || LocalTime.now().isAfter(endTime)) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, this.kickMessage);
        }
    }
}
