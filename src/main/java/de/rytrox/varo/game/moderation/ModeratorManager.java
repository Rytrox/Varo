package de.rytrox.varo.game.moderation;

import de.rytrox.varo.Varo;

import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;

public class ModeratorManager implements Listener {

    private final Varo main;

    public ModeratorManager(@NotNull Varo main) {
        this.main = main;

        main.getCommand("spectate").setExecutor(new SpectateCommand());
        main.getCommand("invsee").setExecutor(new InvseeCommand(main));
        Bukkit.getPluginManager().registerEvents(this, main);
        Bukkit.getPluginManager().registerEvents(new ModeratorTeleporter(main, this), main);
    }

    public boolean isModerator(@NotNull CommandSender sender) {
        return sender.hasPermission("varo.admin.moderator");
    }

    @EventHandler
    public void onModSpawn(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if(isModerator(player)) {
            event.setJoinMessage(null);

            enableModeratorMode(player);
        }
    }

    @EventHandler
    public void onRemoveAgro(EntityTargetLivingEntityEvent event) {
        if(event.getTarget() != null && isModerator(event.getTarget())) {
            event.setCancelled(true);
        }
    }

    private void enableModeratorMode(@NotNull Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);

        // Remove Mods Inventory
        player.getInventory().clear();
        player.updateInventory();
        player.setHealth(20D);
        player.setFoodLevel(20);

        // Add Compass to moderator

        // Disguise Mod for online TeamMembers
        PacketPlayOutPlayerInfo despawnPacket = new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
                ((CraftPlayer) player).getHandle());

        Bukkit.getScheduler().runTaskLater(main, () -> {
            Bukkit.getOnlinePlayers()
                    .stream()
                    .filter((p) -> !this.isModerator(p))
                    .forEach((p) -> {
                        p.hidePlayer(player);

                        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(despawnPacket);
                    });
        }, 2);
    }

    @EventHandler
    public void onModDropItem(PlayerDropItemEvent event) {
        // Mods are not allowed to drop any Items
        if(isModerator(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDenyDamage(EntityDamageEvent event) {
        if(isModerator(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if(isModerator(event.getEntity())) {
            event.setCancelled(true);
            event.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if(isModerator(event.getPlayer()) || isModerator(event.getRightClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if(isModerator(event.getDamager())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(isModerator(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if(isModerator(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent event) {
        if(isModerator(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInvSeeClick(InventoryClickEvent event) {
        if(isModerator(event.getWhoClicked()) && event.getInventory() != null &&
                !event.getWhoClicked().equals(event.getInventory().getHolder())) {
            event.setCancelled(true);
        }
    }
}
