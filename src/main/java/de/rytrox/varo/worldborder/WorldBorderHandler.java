package de.rytrox.varo.worldborder;

import de.rytrox.varo.Varo;
import de.rytrox.varo.gamestate.GameStateHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class WorldBorderHandler {

    private final Varo main;
    private final int intialSize;
    private int currentSize;
    private final Location center;
    private BukkitTask scheduler;

    public WorldBorderHandler(@NotNull Varo main) {
        this.main = main;
        this.intialSize = main.getConfig().getInt("worldborder.intialSize");
        this.currentSize = this.intialSize;

        World world = Bukkit.getWorld(main.getConfig().getString("worldborder.world"));
        int centerX = main.getConfig().getInt("worldborder.center.x");
        int centerZ = main.getConfig().getInt("worldborder.center.z");
        this.center = new Location(world, centerX, 0, centerZ);

        world.getWorldBorder().setCenter(center);
        world.getWorldBorder().setSize(intialSize);

        GameStateHandler.GameState gameState = GameStateHandler.getInstance().getCurrentGameState();

        if(gameState != GameStateHandler.GameState.SETUP
                && gameState != GameStateHandler.GameState.PRE_GAME
                && gameState != GameStateHandler.GameState.POST) {

            startScheduler();
        }
    }

    public void stopScheduler() {
        if(scheduler != null) scheduler.cancel();
    }

    public void startScheduler() {

        if(scheduler != null) {
            this.scheduler.cancel();
        }

        scheduler = Bukkit.getScheduler().runTaskTimerAsynchronously(main, () -> {

            switch(GameStateHandler.getInstance().getCurrentGameState()) {

                case START: return;
                case MAIN: this.currentSize -= this.currentSize > 10 ? 1 : 0; break;
                case FINAL: this.currentSize -= this.currentSize > 10 ? 2 : 0; break;
                case POST:
                    this.currentSize = 10;
                    this.center.getWorld().getWorldBorder().setSize(10);
                    this.stopScheduler();
                    return;
            }

            this.center.getWorld().getWorldBorder().setSize(currentSize, 1);

        }, 0, 20); // per second
    }


}
