package de.rytrox.varo.game.portal;

import de.rytrox.varo.Varo;
import de.rytrox.varo.gamestate.GameStateHandler;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.jetbrains.annotations.NotNull;

public class PortalListener implements Listener {

    private final Varo main;

    public PortalListener(Varo main) {
        this.main = main;
    }

    @EventHandler
    public void onCreate(PortalCreateEvent event) {
        // prevent building nether or end portals in overworld
        if(isOverWorld(event.getWorld()) &&
                main.getGameStateHandler().getCurrentGameState() != GameStateHandler.GameState.SETUP) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTravel(PlayerPortalEvent event) {
        // when travelling from nether to overworld => teleport to world center (spawn)
        if(event.getFrom().getWorld().getEnvironment() == World.Environment.NETHER) {
            event.setCancelled(true);
            event.getPlayer().teleport(main.getWorldBorderHandler().getCenter());
        }
    }

    @EventHandler
    public void onPortalExplode(EntityExplodeEvent event) {
        // The portal cannot be destroyed with explosions in Overworld
        if(isOverWorld(event.getLocation().getWorld()) &&
                main.getGameStateHandler().getCurrentGameState() != GameStateHandler.GameState.SETUP) {
            event.blockList()
                    .removeIf((block) -> block.getType() == Material.PORTAL);
        }
    }

    @EventHandler
    public void onPortalBreak(BlockBreakEvent event) {
        // protect portal-frames in Overworld
        if(isOverWorld(event.getBlock().getWorld()) && isNetherPortalBlock(event.getBlock()) &&
            main.getGameStateHandler().getCurrentGameState() != GameStateHandler.GameState.MAIN) {
            event.setCancelled(true);
        }
    }

    /**
     * Checks if the current world is the overworld where the border is active
     *
     * @param world the world you want to check. Cannot be null
     * @return true, if the world is the world where the border is active. false otherwise
     */
    private boolean isOverWorld(@NotNull World world) {
        return world.equals(main.getWorldBorderHandler().getCenter().getWorld());
    }

    /**
     * Checks if the current block is part of a Nether-Portal
     *
     * @param obsidian the obsidian block
     * @return true if this block is part of a Nether-Portal.
     *         false otherwise
     */
    private boolean isNetherPortalBlock(@NotNull Block obsidian) {
        World world = obsidian.getWorld();

        // check x direction
        return obsidian.getType() == Material.OBSIDIAN && (
                world.getBlockAt(obsidian.getLocation().add(1, 0, 0)).getType() == Material.PORTAL ||
                world.getBlockAt(obsidian.getLocation().add(-1, 0, 0)).getType() == Material.PORTAL ||
                world.getBlockAt(obsidian.getLocation().add(0, 1, 0)).getType() == Material.PORTAL ||
                world.getBlockAt(obsidian.getLocation().add(0, -1, 0)).getType() == Material.PORTAL ||
                world.getBlockAt(obsidian.getLocation().add(0, 0, 1)).getType() == Material.PORTAL ||
                world.getBlockAt(obsidian.getLocation().add(0, 0, -1)).getType() == Material.PORTAL);
    }
}
