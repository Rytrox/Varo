package de.rytrox.varo.game;

import de.rytrox.varo.Varo;
import de.rytrox.varo.gamestate.GameStateHandler;
import de.rytrox.varo.gamestate.events.GamestateChangeEvent;
import de.rytrox.varo.teams.events.TeamMemberLoginEvent;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
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

            // set time and clear weather
            World world = Bukkit.getWorld(main.getConfig().getString("worldborder.world", "world"));
            world.setTime(1000L);
            world.setStorm(false);
            world.setThundering(false);

            Bukkit.getOnlinePlayers()
                    .stream()
                    .filter(player -> !main.getModeratorManager().isModerator(player)) // ignore moderators!
                    .forEach(player -> {
                        player.setGameMode(GameMode.SURVIVAL);
                        player.getInventory().clear();
                        player.setHealth(20);
                        player.setFoodLevel(20);
                    });
        } else if(event.getNext() == GameStateHandler.GameState.POST) {
            Bukkit.getOnlinePlayers()
                    .stream()
                    .filter(player -> !main.getModeratorManager().isModerator(player)) // ignore moderators!
                    .forEach(player -> {
                        player.setGameMode(GameMode.ADVENTURE);
                    });
        } else {
            Bukkit.getOnlinePlayers()
                    .stream()
                    .filter((player) -> !main.getModeratorManager().isModerator(player)) // ignore moderators!
                    .forEach(player -> player.setGameMode(GameMode.ADVENTURE));
        }
    }

    @EventHandler
    public void onTeleportSpawn(TeamMemberLoginEvent event) {
        if(!event.isCancelled() &&
                main.getGameStateHandler().getCurrentGameState() == GameStateHandler.GameState.PRE_GAME &&
                event.getMember().getSpawnPoint() != null) {
            event.getPlayer().teleport(event.getMember().getSpawnPoint().getLocation());
        }
    }

    @EventHandler
    public void onJoin(TeamMemberLoginEvent event) {
        if(!event.isCancelled()) {
            Player player = event.getPlayer();

            // Force Gamemode to survival when game is running and player is not in correct gamemode. Ignore Moderators!
            if (!main.getModeratorManager().isModerator(player)) {

                GameStateHandler.GameState gameState = main.getGameStateHandler().getCurrentGameState();

                if((gameState == GameStateHandler.GameState.MAIN
                        || gameState == GameStateHandler.GameState.FINAL)) {
                } else {
                    player.setGameMode(GameMode.ADVENTURE);
                }
            }
        }
    }
}
