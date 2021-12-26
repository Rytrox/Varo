package de.rytrox.varo.game;

import com.google.gson.JsonObject;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.PlayerTimeStatistic;
import de.rytrox.varo.database.repository.PlayerTimeStatisticRepository;
import de.rytrox.varo.game.events.GameDayEndEvent;
import de.rytrox.varo.gamestate.GameStateHandler;
import de.rytrox.varo.gamestate.events.GamestateChangeEvent;
import de.rytrox.varo.teams.events.TeamMemberSpawnEvent;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class PlayerTimeService implements Listener {

    private final PlayerTimeStatisticRepository playerTimeStatisticRepository;

    private final Map<OfflinePlayer, LocalTime> playerDayTime = new HashMap<>();

    private final Varo main;
    private BukkitTask playerTimeTask;

    private final LocalTime dayLength;
    private final int minAllowedDays;
    private final int maxAllowedDays;

    public PlayerTimeService(@NotNull Varo main) {
        this.main = main;

        this.minAllowedDays = main.getConfig().getInt("game.playertime.min-allowed-days", -3);
        this.maxAllowedDays = main.getConfig().getInt("game.playertime.max-allowed-days", 3);
        this.dayLength = LocalTime.parse(main.getConfig().getString("game.playertime.day-length", "00:15"));
        this.playerTimeStatisticRepository = new PlayerTimeStatisticRepository(main.getDB());
    }

    @EventHandler
    public void onPlayerConnect(TeamMemberSpawnEvent event) {
        // check if the player is not reconnected
        if(!playerDayTime.containsKey(event.getPlayer())) {
            // read his stats from database
            PlayerTimeStatistic statistic = event.getMember().getPlayerTimeStatistic();

            // check if his block is still valid
            if(statistic.isBlocked()) {
                if(statistic.getAvailableDays() < 1) {
                    event.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&',
                            "&8[&6Varo&8] &cDeine Sperre ist noch nicht aufgehoben. \n Versuche es am " +
                                    LocalDate.now().minusDays(statistic.getAvailableDays() - 1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " erneut."));
                    return;
                } else {
                    // Unblock player
                    statistic.setBlocked(false);
                }
            } else {
                statistic.setBlocked(this.minAllowedDays == statistic.getAvailableDays());
                if(statistic.isBlocked()) {
                    event.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&',
                            "&8[&6Varo&8] &cDu wurdest durch Überziehen deiner Spielzeit bis zum " +
                                    LocalDate.now().minusDays(statistic.getAvailableDays() - 1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) +
                                    " gesperrt. \n \n Bitte versuche es dann erneut!"));
                    return;
                }
            }

            // insert into local cache
            statistic.decreaseDays();
            main.getDB().update(statistic);
            this.playerDayTime.put(event.getPlayer(), this.dayLength);
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
            playerTimeTask = Bukkit.getScheduler().runTaskTimerAsynchronously(main, () -> {
                // send time of every player to every Player in action bar
                playerDayTime.entrySet()
                        .stream()
                        .filter((entry) -> entry.getKey().isOnline())
                        .forEach((entry) -> {
                            LocalTime newTime = entry.getValue().minusSeconds(1);

                            playerDayTime.put(entry.getKey(), newTime);

                            // send time to action bar
                            JsonObject message = new JsonObject();
                            message.addProperty("text", ChatColor.translateAlternateColorCodes('&', "&5" +
                                    newTime.format(DateTimeFormatter.ofPattern("hh:mm"))));

                            PacketPlayOutChat chatPacket = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(message.toString()), (byte) 2);
                            ((CraftPlayer)entry.getKey().getPlayer()).getHandle().playerConnection.sendPacket(chatPacket);
                        });
            }, 0, 20L);
        } else if(this.playerTimeTask != null){
            // stop task
            this.playerTimeTask.cancel();
            this.playerTimeTask = null;
        }
    }
}
