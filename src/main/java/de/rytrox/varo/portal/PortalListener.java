package de.rytrox.varo.portal;

import de.rytrox.varo.Varo;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.PortalCreateEvent;

public class PortalListener implements Listener {

    private final Varo main;

    public PortalListener(Varo main) {
        this.main = main;
    }

    @EventHandler
    public void onCreate(PortalCreateEvent event) {
        // prevent building nether or end portals in overworld
        if(event.getWorld().getEnvironment() == World.Environment.NORMAL) {
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

}
