package de.rytrox.varo.worldborder;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.repository.TeamMemberRepository;
import de.rytrox.varo.gamestate.GameStateHandler;
import de.rytrox.varo.resurrection.PlayerResurrectionEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class WorldBorderHandler implements Listener {

    private final Varo main;
    private final int intialSize;
    private double currentSize;

    private final int totalPlayers;
    private int alivePlayers;

    private final Location center;

    public WorldBorderHandler(@NotNull Varo main) {
        this.main = main;

        Bukkit.getPluginManager().registerEvents(this, main);

        this.intialSize = main.getConfig().getInt("worldborder.intialSize");
        this.currentSize = this.intialSize;

        TeamMemberRepository teamMemberRepository = new TeamMemberRepository(main.getDB());
        this.totalPlayers = teamMemberRepository.getTotalPlayerAmount();
        this.alivePlayers = teamMemberRepository.getAlivePlayerAmount();

        World world = Bukkit.getWorld(main.getConfig().getString("worldborder.world"));
        int centerX = main.getConfig().getInt("worldborder.center.x");
        int centerZ = main.getConfig().getInt("worldborder.center.z");
        this.center = new Location(world, centerX, 0, centerZ);

        world.getWorldBorder().setCenter(center);
        world.getWorldBorder().setSize(intialSize);

    }

    public void updateWorldBorder() {

        switch(GameStateHandler.getInstance().getCurrentGameState()) {

            case SETUP:
            case PRE_GAME:
            case START: return;
            case MAIN: this.currentSize = (alivePlayers / ((double) totalPlayers)) * intialSize; break;
            case FINAL: this.currentSize -= this.currentSize > 24 ? 0.1 : 0; break;
            case POST:
                this.currentSize = 10;
                this.center.getWorld().getWorldBorder().setSize(10);
                return;
        }

        this.center.getWorld().getWorldBorder().setSize(currentSize, 1);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        this.alivePlayers--;
    }

    @EventHandler
    public void onResurrect(PlayerResurrectionEvent event) {
        this.alivePlayers++;
    }


}
