package de.rytrox.varo.game;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.repository.TeamMemberRepository;
import de.rytrox.varo.gamestate.GameStateHandler;
import de.rytrox.varo.gamestate.events.GamestateChangeEvent;
import de.rytrox.varo.teams.events.TeamMemberSpawnEvent;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Service for managing Ingame-Stuff
 */
public class GameService implements Listener {

    private final Varo main;

    private final TeamMemberRepository teamMemberRepository;

    public GameService(@NotNull Varo main) {
        this.main = main;

        this.teamMemberRepository = new TeamMemberRepository(main.getDB());

        Bukkit.getPluginManager().registerEvents(new GameTimeService(main), main);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(main), main);
    }

    @EventHandler
    public void onGameStart(GamestateChangeEvent event) {
        if (event.getNext() == GameStateHandler.GameState.MAIN || event.getNext() == GameStateHandler.GameState.FINAL) {
            Bukkit.getOnlinePlayers()
                    .stream()
                    .filter((player) -> !main.getModeratorManager().isModerator(player)) // ignore moderators!
                    .forEach(player -> player.setGameMode(GameMode.SURVIVAL));
        } else {
            Bukkit.getOnlinePlayers()
                    .stream()
                    .filter((player) -> !main.getModeratorManager().isModerator(player)) // ignore moderators!
                    .forEach(player -> player.setGameMode(GameMode.ADVENTURE));
        }
    }

    @EventHandler
    public void onTeleportSpawn(GamestateChangeEvent event) {
        if(event.getNext() == GameStateHandler.GameState.PRE_GAME) {
            Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
                teamMemberRepository.getOnlineMembers()
                        .forEach((member) ->
                            Optional.ofNullable(member.getSpawnPoint())
                                .ifPresent((spawn) ->
                                    Objects.requireNonNull(member.getPlayer())
                                           .teleport(member.getSpawnPoint().getLocation())
                                )
                        );
            });
        }
    }

    @EventHandler
    public void onJoin(TeamMemberSpawnEvent event) {
        if(!event.isCancelled()) {
            Player player = event.getPlayer();

            // Force Gamemode to survival when game is running and player is not in correct gamemode. Ignore Moderators!
            if (!main.getModeratorManager().isModerator(player) &&
                    (main.getGameStateHandler().getCurrentGameState() == GameStateHandler.GameState.MAIN ||
                            main.getGameStateHandler().getCurrentGameState() == GameStateHandler.GameState.FINAL)) {
                player.setGameMode(GameMode.SURVIVAL);
            }
        }
    }
}
