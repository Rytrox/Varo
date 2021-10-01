package de.rytrox.varo.database.repository;

import de.rytrox.varo.database.entity.Team;
import de.rytrox.varo.database.entity.TeamItem;

import io.ebean.Database;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TeamItemRepository {

    private final Database database;

    public TeamItemRepository(@NotNull Database database) {
        this.database = database;
    }

    /**
     * Selects the Team-Inventory of a Team
     * Returns an empty list when the player is in no team.
     *
     * NOTE: Please use this method outside the Main-Thread
     *
     * @param team the uuid of the player
     * @return the
     */
    @Nullable
    public List<TeamItem> getTeamItems(@NotNull Team team) {
        // search Team
        return database.find(TeamItem.class)
                .where()
                .eq("owner", team)
                .orderBy("slot")
                .findList();
    }

    public void save(@NotNull Team team, @NotNull Inventory inventory, int maxSlotSize) throws IOException {
        List<TeamItem> list = new ArrayList<>(team.getItems())
                .stream()
                .sorted(Comparator.comparingInt(TeamItem::getSlot))
                .collect(Collectors.toList());

        for(int i = 0; i < maxSlotSize; i++) {
            ItemStack item = inventory.getItem(i);

            if(i < list.size()) {
                // update already existing slots in database
                TeamItem teamItem = list.get(i);

                teamItem.setItemStack(item);
            } else {
                // create not existing slot and put it into database
                TeamItem teamItem = new TeamItem();
                teamItem.setItemStack(item);
                teamItem.setOwner(team);
                teamItem.setSlot(i);

                list.add(teamItem);
            }
        }

        team.setItems(list);
        database.update(team);
    }
}
