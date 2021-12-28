package de.rytrox.varo.game;

import com.google.gson.JsonObject;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.PlayerTimeStatistic;
import de.rytrox.varo.database.repository.PlayerTimeStatisticRepository;
import de.rytrox.varo.game.events.GameDayEndEvent;
import de.rytrox.varo.gamestate.GameStateHandler;
import de.rytrox.varo.gamestate.events.GamestateChangeEvent;
import de.rytrox.varo.teams.events.TeamMemberLoginEvent;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerTimeService implements Listener {

    private final PlayerTimeStatisticRepository playerTimeStatisticRepository;

    private final Map<UUID, Duration> playerDayTime = new LinkedHashMap<>();

    private final Varo main;
    private final GameStateHandler gameStateHandler;

    private BukkitTask playerTimeTask;

    private final LocalTime dayLength;
    private final int allowedSkipDays;
    private final int maxAllowedDays;

    public PlayerTimeService(@NotNull Varo main) {
        this.main = main;
        this.gameStateHandler = main.getGameStateHandler();

        this.allowedSkipDays = main.getConfig().getInt("game.playertime.allowed-skip-days", 2);
        this.maxAllowedDays = main.getConfig().getInt("game.playertime.max-allowed-days", 3);
        this.dayLength = LocalTime.parse(main.getConfig().getString("game.playertime.day-length", "00:15"));
        this.playerTimeStatisticRepository = new PlayerTimeStatisticRepository(main.getDB());
    }

    @EventHandler
    public void onPlayerConnect(TeamMemberLoginEvent event) {
        // Do nothing when game has not been started yet
        if(gameStateHandler.getCurrentGameState() != GameStateHandler.GameState.MAIN)
            return;

        // check if the player is not reconnected
        if(!playerDayTime.containsKey(event.getPlayer().getUniqueId())) {
            // read his stats from database
            PlayerTimeStatistic statistic = event.getMember().getPlayerTimeStatistic();

            // check if his block is still valid
            if(statistic.isBlocked()) {
                if(statistic.getAvailableDays() < 0) {
                    event.setCancelled(true);
                    event.setCancelMessage(ChatColor.translateAlternateColorCodes('&',
                            "&8[&6Varo&8] &cDeine Sperre ist noch nicht aufgehoben. \n Versuche es am " +
                                    LocalDate.now().minusDays(statistic.getAvailableDays()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " erneut."));

                    return;
                } else {
                    // Unblock player
                    statistic.setBlocked(false);
                }
            } else {
                statistic.setBlocked(statistic.getAvailableDays() < -this.allowedSkipDays);
                if(statistic.isBlocked()) {
                    event.setCancelled(true);
                    event.setCancelMessage(ChatColor.translateAlternateColorCodes('&',
                            "&8[&6Varo&8] &cDu wurdest durch Überziehen deiner Spielzeit bis zum " +
                                    LocalDate.now().minusDays(statistic.getAvailableDays()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) +
                                    " gesperrt. \n \n Bitte versuche es dann erneut!"));
                    main.getDB().update(statistic);
                    return;
                }
            }

            // insert into local cache
            statistic.decreaseDays();
            main.getDB().update(statistic);

            this.playerDayTime.put(event.getPlayer().getUniqueId(), Duration.between(LocalTime.now(), LocalTime.now()
                    .plusHours(this.dayLength.getHour())
                    .plusMinutes(this.dayLength.getMinute())
                    .plusSeconds(this.dayLength.getSecond()))
            );
        }
    }

    @EventHandler
    public void onGameDayEnd(GameDayEndEvent event) {
        // clear list of game time
        this.playerDayTime.clear();
        // increase all days except those that are already maxed
        this.playerTimeStatisticRepository.increaseDays(this.maxAllowedDays);
    }

    @EventHandler
    public void onGameStateChange(GamestateChangeEvent event) {
        // start when next gamestate is ingame
        if(event.getNext() == GameStateHandler.GameState.MAIN) {
            // Add all online players to the list
            Bukkit.getOnlinePlayers().forEach(player -> this.playerDayTime.put(player.getUniqueId(), Duration.between(LocalTime.now(), LocalTime.now()
                    .plusHours(this.dayLength.getHour())
                    .plusMinutes(this.dayLength.getMinute())
                    .plusSeconds(this.dayLength.getSecond()))
            ));

            playerTimeTask = Bukkit.getScheduler().runTaskTimerAsynchronously(main, () -> {
                // send time of every player to every Player in action bar
                playerDayTime.entrySet()
                        .stream()
                        .filter((entry) -> Bukkit.getPlayer(entry.getKey()) != null)
                        .collect(Collectors.toList())
                        .forEach((entry) -> {
                            Duration newTime = entry.getValue().minusSeconds(1);

                            if(!newTime.equals(Duration.ZERO)) {
                                playerDayTime.put(entry.getKey(), newTime);

                                // send time to action bar
                                JsonObject message = new JsonObject();
                                message.addProperty("text", ChatColor.translateAlternateColorCodes('&', "&5Verbleibende Spielzeit: " +
                                        DurationFormatUtils.formatDuration(newTime.toMillis(), "mm:ss")));

                                PacketPlayOutChat chatPacket = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(message.toString()), (byte) 2);
                                ((CraftPlayer)Bukkit.getPlayer(entry.getKey())).getHandle().playerConnection.sendPacket(chatPacket);
                            } else {
                                playerDayTime.remove(entry.getKey());

                                Bukkit.getScheduler().runTask(main, () ->
                                    Bukkit.getPlayer(entry.getKey()).kickPlayer(ChatColor.translateAlternateColorCodes('&',
                                            "&8[&6Varo&8] &cDein Spieltag ist vorbei. Morgen geht es weiter!")
                                    )
                                );
                            }
                        });
            }, 0, 20L);
        } else if(this.playerTimeTask != null){
            // stop task
            this.playerTimeTask.cancel();
            this.playerTimeTask = null;
        }
    }
}
