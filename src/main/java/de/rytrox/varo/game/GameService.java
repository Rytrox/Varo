package de.rytrox.varo.game;

import de.rytrox.varo.Varo;
import de.rytrox.varo.gamestate.GameStateHandler;
import de.rytrox.varo.gamestate.events.GamestateChangeEvent;
import de.rytrox.varo.teams.events.TeamMemberSpawnEvent;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * Service for managing Ingame-Stuff
 */
public class GameService implements Listener {

    private final Varo main;

    public GameService(@NotNull Varo main) {
        this.main = main;

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
    public void onTeleportSpawn(TeamMemberSpawnEvent event) {
        if(!event.isCancelled() &&
                main.getGameStateHandler().getCurrentGameState() == GameStateHandler.GameState.PRE_GAME &&
                event.getMember().getSpawnPoint() != null) {
            event.getPlayer().teleport(event.getMember().getSpawnPoint().getLocation());
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
