package de.rytrox.varo.moderation;

import de.rytrox.varo.Varo;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class ModeratorManager implements Listener {

    private final Varo main;

    public ModeratorManager(@NotNull Varo main) {
        this.main = main;

        main.getCommand("spectate").setExecutor(new SpectateCommand());
    }

    @EventHandler
    public void onModSpawn(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if(player.hasPermission("varo.admin.moderator")) {
            event.setJoinMessage(null);

            // Remove Mods Inventory
            player.getInventory().clear();
            player.updateInventory();
            player.setGameMode(GameMode.SPECTATOR);

            // Disguise Mod for online TeamMembers
            PacketPlayOutPlayerInfo despawnPacket = new PacketPlayOutPlayerInfo(
                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
                    ((CraftPlayer) player).getHandle());

            Bukkit.getScheduler().runTaskLater(main, () -> {
                Bukkit.getOnlinePlayers()
                        .stream()
                        .filter((p) -> !p.hasPermission("varo.admin.moderator"))
                        .forEach((p) -> {
                            p.hidePlayer(player);

                            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(despawnPacket);
                        });
            }, 2);
        }
    }

    @EventHandler
    public void onModDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        // Mods are not allowed to drop any Items
        if(player.hasPermission("varo.admin.moderator")) {
            event.setCancelled(true);
        }
    }
}
