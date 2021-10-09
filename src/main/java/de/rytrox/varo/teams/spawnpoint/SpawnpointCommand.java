package de.rytrox.varo.teams.spawnpoint;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.repository.TeamMemberRepository;
import de.rytrox.varo.utils.CommandHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SpawnpointCommand implements TabExecutor {

    private final SpawnPointService service;

    public SpawnpointCommand(@NotNull Varo main, @NotNull TeamMemberRepository teamMemberRepository) {
        this.service = new SpawnPointService(main, teamMemberRepository);

        Bukkit.getPluginManager().registerEvents(this.service, main);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if(args.length == 2 && args[0].equalsIgnoreCase("set")) {
                service.setSpawnPoint(player, args[1]);
            } else sendHelp(commandSender);
        } else commandSender.sendMessage(ChatColor.RED + "Nur Spieler können Spawnpoints setzen");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        return null;
    }

    private void sendHelp(@NotNull CommandSender commandSender) {
        commandSender.sendMessage(CommandHelper.formatCommandHeader("/spawnpoint"));
        commandSender.sendMessage(CommandHelper.formatCommandExplanation("/spawnpoint set <player>", "Setzt den Spawnpoint eines bestimmten Spielers"));
    }
}
