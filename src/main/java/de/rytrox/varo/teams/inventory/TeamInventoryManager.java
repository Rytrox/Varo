package de.rytrox.varo.teams.inventory;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.Team;
import de.rytrox.varo.database.entity.TeamItem;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.database.enums.PlayerStatus;
import de.rytrox.varo.database.repository.TeamItemRepository;
import de.rytrox.varo.database.repository.TeamMemberRepository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class TeamInventoryManager implements Listener {

    private final Map<Team, Inventory> openTeamInventories = new HashMap<>();

    private final Varo main;
    private final TeamItemRepository teamItemRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final int maxRowsInTeamInventory;

    public TeamInventoryManager(@NotNull Varo main,
                                @NotNull TeamMemberRepository teamMemberRepository) {
        this.main = main;

        main.getCommand("teaminventory").setAliases(Collections.singletonList("teaminv"));
        main.getCommand("teaminventory").setExecutor(new TeamInventoryCommand(this));

        this.teamItemRepository = new TeamItemRepository(main.getDB());
        this.teamMemberRepository = teamMemberRepository;
        this.maxRowsInTeamInventory = main.getConfig().getInt("teams.inventory.rows", 1);
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            openTeamInventories.entrySet()
                    .stream()
                    .filter(entry -> event.getInventory().equals(entry.getValue()))
                    .findAny()
                    .ifPresent((entry) -> {
                        // save inventory in database when nobody views this inventory
                        if(entry.getValue().getViewers().isEmpty()) {
                            openTeamInventories.remove(entry.getKey());

                            try {
                                teamItemRepository.save(entry.getKey(), entry.getValue());
                            } catch (IOException e) {
                                main.getLogger().log(Level.WARNING, e, () -> "Unhandled exception while saving TeamInventory of " + entry.getKey().getName());
                            }
                        }
                    });
        });
    }

    /**
     * Opens the Team inventory of the Executor
     *
     * @param executor the executor
     */
    public void openTeamInventory(@NotNull HumanEntity executor) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            TeamMember member = teamMemberRepository.getPlayerByUUID(executor.getUniqueId());

            // block invalid use
            if(member == null || member.getStatus() != PlayerStatus.ALIVE) {
                executor.sendMessage(ChatColor.RED + "Nur Spieler, die am Spiel teilnehmen und am Leben sind, dürfen das Teaminventar nutzen");
                return;
            }

            Team team = member.getTeam();
            if(team != null) {
                Inventory inventory = openTeamInventories.get(team);

                // check if inventory is cached
                if(inventory == null) {
                    // read from database
                    List<TeamItem> items = teamItemRepository.getTeamItems(team);

                    if(items != null) {
                        inventory = Bukkit.createInventory(null, 9 * maxRowsInTeamInventory,
                                ChatColor.translateAlternateColorCodes('&', String.format("&7Team-Inventar von &5Team &d%s", team.getName())));

                        final Inventory finalInventory = inventory;
                        items.stream()
                                .filter((element) -> element.getSlot() < finalInventory.getSize())
                                .forEachOrdered((element) -> {
                                    try {
                                        finalInventory.setItem(element.getSlot(), Optional.ofNullable(element.getItemStack())
                                                .orElse(new ItemStack(Material.AIR)));
                                    } catch (IOException e) {
                                        main.getLogger().log(Level.WARNING, "IOException while converting database data", e);
                                    } catch (ClassNotFoundException e) {
                                        main.getLogger().log(Level.WARNING, "Unable to find Class org.bukkit.item.ItemStack", e);
                                    }
                                });

                        // cache result
                        this.openTeamInventories.put(team, finalInventory);
                    }
                }

                executor.openInventory(inventory);
            } else
                executor.sendMessage(ChatColor.RED + "Du gehörst keinem Team an und hast daher kein Teaminventory");
        });
    }
}
