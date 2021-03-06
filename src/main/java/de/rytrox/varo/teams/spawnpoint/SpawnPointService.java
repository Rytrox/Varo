package de.rytrox.varo.teams.spawnpoint;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.SpawnPoint;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.database.enums.PlayerStatus;
import de.rytrox.varo.database.repository.TeamMemberRepository;
import de.rytrox.varo.gamestate.GameStateHandler;
import de.rytrox.varo.teams.events.TeamMemberLoginEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.logging.Level;

public class SpawnPointService implements Listener {

    private final Varo main;

    private final TeamMemberRepository teamMemberRepository;

    public SpawnPointService(@NotNull Varo main, @NotNull TeamMemberRepository teamMemberRepository) {
        this.main = main;
        this.teamMemberRepository = teamMemberRepository;
    }

    public void setSpawnPoint(Player executor, String targetName) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            // get Target
            TeamMember member = teamMemberRepository.getPlayer(Bukkit.getOfflinePlayer(targetName));

            if(member != null) {
                SpawnPoint point = Optional.ofNullable(member.getSpawnPoint())
                        .orElse(new SpawnPoint());

                point.setLocation(executor.getLocation());
                member.setSpawnPoint(point);

                main.getDB().update(member);
                executor.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        String.format("&8[&6Varo&8] &7Du hast den Spawnpoint von %s erfolgreich gesetzt!", member.getOfflinePlayer().getName())));
                main.getLogger().log(Level.INFO, String.format("Player %s sets Spawnpoint of %s to %s", executor.getName(), member.getOfflinePlayer().getName(), point));
            } else executor.sendMessage(ChatColor.RED + "Dieser Spieler ist nicht bekannt");
        });
    }

    /**
     * Teleport the player when the player joins and the game is running
     *
     * @param event the TeamMemberSpawnEvent
     */
    @EventHandler
    public void onSpawn(TeamMemberLoginEvent event) {
        Player player = event.getPlayer();

        // Only teleport if the game is running and the player is alive
        if(event.getMember().getStatus() == PlayerStatus.ALIVE &&
                main.getGameStateHandler().getCurrentGameState() == GameStateHandler.GameState.PRE_GAME) {

            Location spawnPoint = main.getWorldBorderHandler().getCenter();

            Bukkit.getScheduler().runTask(main, () -> player.teleport(spawnPoint));
        }
    }
}
