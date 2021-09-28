package de.rytrox.varo.worldborder;

import de.rytrox.varo.Varo;
import de.rytrox.varo.utils.GameStateHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class WorldBorderHandler {

    private static WorldBorderHandler instance;

    BukkitTask scheduler;

    private WorldBorderHandler() {
        int initialSize = 4000;

        GameStateHandler.GameState gameState = JavaPlugin.getPlugin(Varo.class).getGameStateHandler().getCurrentGameState();

        if(gameState != GameStateHandler.GameState.SETUP
            && gameState != GameStateHandler.GameState.PRE_GAME
            && gameState != GameStateHandler.GameState.POST) {

            startScheduler();
        }
    }

    public static WorldBorderHandler getInstance() {
        if(instance == null) {
            instance = new WorldBorderHandler();
        }
        return instance;
    }

    public void stopScheduler() {
        if(scheduler != null) scheduler.cancel();
    }

    private void startScheduler() {

        Varo main = JavaPlugin.getPlugin(Varo.class);

        scheduler = Bukkit.getScheduler().runTaskTimerAsynchronously(main, () -> {

            switch(main.getGameStateHandler().getCurrentGameState()) {

                case START: break;
                case MAIN: break;
                case FINAL: break;
                case POST: break;

            }

        }, 0, 20);
    }


}
