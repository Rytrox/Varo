package de.rytrox.varo.game.moderation;

import de.rytrox.varo.Varo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ModeratorTeleporter implements Listener {

    private static final ItemStack TELEPORTER = new ItemStack(Material.COMPASS);

    private final Varo main;
    private final ModeratorManager manager;

    static {
        ItemMeta meta = TELEPORTER.getItemMeta();

        meta.setDisplayName(ChatColor.DARK_PURPLE + "Teleporter");
        meta.addEnchant(Enchantment.DURABILITY, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        TELEPORTER.setItemMeta(meta);
    }

    public ModeratorTeleporter(@NotNull Varo main, @NotNull ModeratorManager manager) {
        this.main = main;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemGet(PlayerJoinEvent event) {
        if(manager.isModerator(event.getPlayer())) {
            event.getPlayer().getInventory().addItem(TELEPORTER);
        }
    }

    @EventHandler
    public void onTeleporterUse(PlayerInteractEvent event) {
        if(TELEPORTER.equals(event.getItem()) &&
                (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            openNavigationInventory(event.getPlayer());
        }
    }

    @EventHandler
    public void onTeleporterMenuClick(InventoryClickEvent event) {
        if(event.getClickedInventory() != null &&
                event.getClickedInventory().getName().equals(ChatColor.DARK_PURPLE + "Spielerteleporter")) {
            event.setResult(Event.Result.DENY);
            event.setCancelled(true);

            Optional.ofNullable(event.getCurrentItem())
                    .filter((item) -> item.getItemMeta() instanceof SkullMeta)
                    .ifPresent((skull) -> {
                        HumanEntity player = event.getWhoClicked();
                        Player target = Bukkit.getPlayer(((SkullMeta) skull.getItemMeta()).getOwner());

                        if(player instanceof Player) {
                            ((Player) event.getWhoClicked())
                                    .playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1F, 1F);
                        }

                        player.teleport(target);
                        player.closeInventory();
                        player.sendMessage(
                                ChatColor.translateAlternateColorCodes('&',
                                        String.format("&8[&6Varo&8] &7Du wurdest zu Spieler %s teleportiert", skull.getItemMeta().getDisplayName())));
                    });
        }
    }

    private void openNavigationInventory(@NotNull Player entity) {
        Inventory inventory = Bukkit.createInventory(null,
                (Bukkit.getOnlinePlayers().size() / 9 + 1) * 9,
                ChatColor.DARK_PURPLE + "Spielerteleporter");

        inventory.addItem(
                Bukkit.getOnlinePlayers()
                        .stream()
                        .filter((player) -> !manager.isModerator(player))
                        .sorted((a, b) -> main.getScoreBoardManager().getTablistName(a).compareTo(main.getScoreBoardManager().getTablistName(b)))
                        .map((player) -> {
                            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

                            SkullMeta meta = (SkullMeta) skull.getItemMeta();
                            meta.setOwner(player.getName());
                            meta.setDisplayName(main.getScoreBoardManager().getTablistName(player));

                            skull.setItemMeta(meta);

                            return skull;
                        }).toArray(ItemStack[]::new)
        );

        entity.openInventory(inventory);
        entity.playSound(entity.getLocation(), Sound.PORTAL, 1F, 1F);
    }
}
