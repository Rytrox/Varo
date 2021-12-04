package de.rytrox.varo.worldborder;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.repository.TeamMemberRepository;
import de.rytrox.varo.discord.MessageService;
import de.rytrox.varo.gamestate.GameStateHandler;
import de.rytrox.varo.resurrection.PlayerResurrectionEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class WorldBorderHandler implements Listener {

    private final Varo main;
    private final double intialSize;
    private final double minSize;
    private final double shrinkSpeed;

    private final int totalPlayers;
    private int alivePlayers;

    private final Location center;
    private BukkitTask suddenDeathTask;

    public WorldBorderHandler(@NotNull Varo main) {
        this.main = main;

        Bukkit.getPluginManager().registerEvents(this, main);

        this.intialSize = main.getConfig().getDouble("worldborder.intialSize");
        this.minSize = main.getConfig().getDouble("worldborder.suddenDeath.minimal");
        this.shrinkSpeed = main.getConfig().getDouble("worldborder.suddenDeath.speed");
        double currentSize = this.intialSize; // TODO VARO-28 read from json

        TeamMemberRepository teamMemberRepository = new TeamMemberRepository(main.getDB());
        this.totalPlayers = teamMemberRepository.getTotalPlayerAmount();
        this.alivePlayers = teamMemberRepository.getAlivePlayerAmount();

        World world = Bukkit.getWorld(main.getConfig().getString("worldborder.world"));
        int centerX = main.getConfig().getInt("worldborder.center.x");
        int centerZ = main.getConfig().getInt("worldborder.center.z");
        this.center = new Location(world, centerX, 0, centerZ);

        world.getWorldBorder().setCenter(center);
        world.getWorldBorder().setSize(currentSize);
        world.getWorldBorder().setDamageAmount(0.1D);

        // register worldborder command
        main.getCommand("varoworldborder").setExecutor(new WorldBorderCommand(main));
    }

    /**
     * Checks if sudden death mode is active or not
     * @return result of check
     */
    public boolean isSuddenDeath() {
        return suddenDeathTask.getTaskId() != -1;
    }

    /**
     * Toggles the SuddenDeath Mode
     * @return true if SuddenDeath has been activated <br>
     * false if SuddenDeath has been deactivated
     */
    public boolean toggleSuddenDeath() {

        if(suddenDeathTask.getTaskId() == -1) {
            this.suddenDeathTask = Bukkit.getScheduler().runTaskTimer(main, () -> {
                if(getSize() > minSize) {
                    setSize(getSize() - shrinkSpeed);
                }
            }, 20, 20);
            return true;
        }

        this.suddenDeathTask.cancel();
        return false;
    }

    /**
     * updates the size of the worldborder
     */
    public void updateWorldBorder() {
        double newSize;

        switch(GameStateHandler.getInstance().getCurrentGameState()) {
            case MAIN: newSize = (alivePlayers / ((double) totalPlayers)) * intialSize; break;
            case POST: newSize = 10; break;
            default: newSize = this.intialSize;
        }

        setSize(newSize);
    }

    /**
     * Sets the size of the worldborder
     */
    public void setSize(double size) {
        this.center.getWorld().getWorldBorder().setSize(size);
        // TODO VARO-28 save size in json
    }

    /**
     * Gets the size of the worldborder
     * @return current worldborder size
     */
    public double getSize() {
        return this.center.getWorld().getWorldBorder().getSize();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        this.alivePlayers--;
    }

    @EventHandler
    public void onResurrect(PlayerResurrectionEvent event) {
        this.alivePlayers++;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.checkSpawnPosition(event.getPlayer());
    }

    /**
     * checks if a given player has spawned within the worldborder and teleports them to the center if they are not
     * @param player Player you want to check
     */
    private void checkSpawnPosition(Player player) {
        if(!isInsideWorldBorder(player)) {
            player.teleport(center);
            MessageService.getInstance().leakPlayerCoordinates(player, MessageService.CoordinateLeakReason.SPAWN_OUTSIDE_BORDER);
        }
    }

    /**
     * Checks if a given player is inside the worldborder
     * @param player The player you want to check
     * @return result of check (also true if the player is in another world)
     */
    private boolean isInsideWorldBorder(Player player) {
        Location playerLocation = player.getLocation();

        // check if player is in the same world as the worldborder
        if(!(playerLocation.getWorld().getName().equals(center.getWorld().getName()))) {
            return true;
        }

        double worldBorderSize = center.getWorld().getWorldBorder().getSize();

        double maxX = center.getX() + worldBorderSize/2;
        double maxY = center.getY() + worldBorderSize/2;
        double minX = center.getX() + worldBorderSize/2;
        double minY = center.getY() + worldBorderSize/2;

        double playerX = playerLocation.getX();
        double playerY = playerLocation.getY();

        return playerX <= maxX && playerX >= minX && playerY <= maxY && playerY >= minY;
    }

}
