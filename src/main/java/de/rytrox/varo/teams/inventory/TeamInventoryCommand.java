package de.rytrox.varo.teams.inventory;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

public class TeamInventoryCommand implements CommandExecutor {

    private final TeamInventoryManager manager;

    public TeamInventoryCommand(@NotNull TeamInventoryManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof HumanEntity) {
            HumanEntity player = (HumanEntity) commandSender;

            // open Inventory
            manager.openTeamInventory(player);
        } else commandSender.sendMessage(ChatColor.RED + "Nur Spieler dürfen diesen Befehl ausführen");

        return true;
    }
}
