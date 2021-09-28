package de.rytrox.varo.listener;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerDeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        // get Player
        Player player = event.getEntity();

        // create Skull item
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3); // Create a new ItemStack of the Player Head type.
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta(); // Get the created item's ItemMeta and cast it to SkullMeta so we can access the skull properties
        skullMeta.setOwner(player.getName()); // Set the skull's owner so it will adapt the skin of the provided username (case sensitive).
        skull.setItemMeta(skullMeta); // Apply the modified meta to the initial created item

        // drop item at death location
        Item skullItem = player.getLocation().getWorld().dropItemNaturally(player.getLocation(), skull);
    }

}
