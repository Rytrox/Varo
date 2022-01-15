package de.rytrox.varo.game.resurrection;

import de.rytrox.varo.teams.events.TeamMemberDeathEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerSkullDropService implements Listener {

    @EventHandler
    public void onDeath(TeamMemberDeathEvent event) {
        // get Player
        Player player = event.getPlayer();

        // create Skull item
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwner(player.getName());
        skullMeta.setDisplayName("Kopf von " + player.getName());
        skull.setItemMeta(skullMeta);

        // drop item at death location
        player.getLocation().getWorld().dropItemNaturally(player.getLocation(), skull);
    }

}
