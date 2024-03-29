package de.rytrox.varo.game.resurrection;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.database.enums.PlayerStatus;
import de.rytrox.varo.message.MessageService;
import de.rytrox.varo.utils.ParticleUtils;

import net.minecraft.server.v1_8_R3.EnumParticle;

import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class PlayerResurrectionListener implements Listener {

    private final Varo main;

    public PlayerResurrectionListener(@NotNull Varo main) {
        this.main = main;
    }

    @EventHandler
    public void onExplosion(EntityDamageByBlockEvent event) {
        // cancel damage with unknown source
        if(event.getDamager() == null)
            event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {

        Player player = event.getPlayer();

        Item drop = event.getItemDrop();

        // check if dropped item is a golden apple
        if(drop.getItemStack().getType() != Material.GOLDEN_APPLE)
            return;

        // calculate drop destination
        PlayerResurrectionService.DropDestination dropDestination = PlayerResurrectionService.evaluateDropDestination(drop);

        // check if drop location could have been evaluated
        if(dropDestination == null)
            return;

        // check if evaluated drop location or surrounding blocks contain a flower
        Location flowerLocation = PlayerResurrectionService.checkForFlower(dropDestination.getDestination());

        if(flowerLocation == null)
            return;

        // wait for the apple to arrive at the destination
        Bukkit.getScheduler().runTaskLater(main, () -> {

            // check if there is any player to resurrect
            TeamMember resurrectableTeamMember = PlayerResurrectionService.resurrectionIsPossibleABoolean(player);
            if(resurrectableTeamMember == null)
                return;

            // get Player who should be resurrected
            Player resurrectablePlayer = resurrectableTeamMember.getPlayer();
            if(resurrectablePlayer == null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&8[&6Varo&8] &cDein Teammate muss online sein, um wiederbelebt werden zu können")
                );
                return;
            }

            // check if scheme has been build correctly
            if(PlayerResurrectionService.checkScheme(flowerLocation)) {
                // Find and delete Skulls and Sign
                for(int dx = -1; dx <= 1; dx++) {
                    for(int dz = -1; dz <= 1; dz++) {
                        flowerLocation.getBlock().getLocation().add(dx, 0, dz).getBlock().setType(Material.AIR);
                    }
                }

                // strike lightning effect and create explosion
                drop.remove();
                flowerLocation.getWorld().strikeLightningEffect(flowerLocation.getBlock().getLocation());
                flowerLocation.getWorld().createExplosion(
                        flowerLocation.getBlockX(),
                        flowerLocation.getBlockY(),
                        flowerLocation.getBlockZ(),
                        4F, false, true);

                // resurrect player and teleport them to the center of explosion
                resurrectableTeamMember.setStatus(PlayerStatus.ALIVE);
                JavaPlugin.getPlugin(Varo.class).getDB().update(resurrectableTeamMember);

                flowerLocation.setDirection(new Vector(
                        player.getLocation().getX() - resurrectablePlayer.getLocation().getX(),
                        player.getLocation().getY() - resurrectablePlayer.getLocation().getY(),
                        player.getLocation().getZ() - resurrectablePlayer.getLocation().getZ()
                ));
                resurrectablePlayer.teleport(flowerLocation);

                resurrectablePlayer.setGameMode(GameMode.SURVIVAL);
                resurrectablePlayer.setHealth(20);
                resurrectablePlayer.getInventory().addItem(drop.getItemStack());

                // play sound
                Bukkit.getOnlinePlayers().forEach(ap -> ap.playSound(ap.getLocation(), Sound.WITHER_DEATH, 1, 1));

                // send Log-Message

                main.getMessageService().writeMessage(ChatColor.translateAlternateColorCodes('&',
                        String.format("&8[&6Varo&8] %s &7hat seinen Teammate &d%s &awiederbelebt",
                                main.getScoreBoardManager().getChatName(player),
                                resurrectablePlayer.getName())
                ), MessageService.DiscordColor.RED);

                Bukkit.getPluginManager().callEvent(new PlayerResurrectionEvent(resurrectablePlayer, event.getPlayer()));
            } else {
                // if scheme has not been build correctly, tell player by creating some effects

                // create particles
                new ParticleUtils(EnumParticle.SMOKE_LARGE,
                        flowerLocation,
                        true,
                        Float.MIN_VALUE,
                        2,
                        Float.MIN_VALUE, 0.1f,
                        10)
                        .sendAll();
                // play sound
                flowerLocation.getWorld().playSound(flowerLocation, Sound.FIZZ, 1, 1);
            }

        }, Math.round(dropDestination.getTime() * 20));
    }


}
