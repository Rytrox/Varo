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
        this.currentSize = this.intialSize; // TODO should be one of those single-value that should be saved in a json later (VARO-28)

        TeamMemberRepository teamMemberRepository = new TeamMemberRepository(main.getDB());
        this.totalPlayers = teamMemberRepository.getTotalPlayerAmount();
        this.alivePlayers = teamMemberRepository.getAlivePlayerAmount();

        World world = Bukkit.getWorld(main.getConfig().getString("worldborder.world"));
        int centerX = main.getConfig().getInt("worldborder.center.x");
        int centerZ = main.getConfig().getInt("worldborder.center.z");
        this.center = new Location(world, centerX, 0, centerZ);

        world.getWorldBorder().setCenter(center);
        world.getWorldBorder().setSize(intialSize);
        world.getWorldBorder().setDamageAmount(0.1D);

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
