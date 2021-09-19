package de.rytrox.varo.teams;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

/**
 * Command for Teams:
 *
 * - /teams add <name> -> Creates a new Team
 * - /teams modify <name> displayname <displayname> -> Sets the displayname
 * - /teams members <name> add <playername> -> Adds a player to a team
 * - /teams members <name> remove <playername> -> Removes a player from a team
 *
 */
public class TeamsCommand implements TabExecutor {

    private final TeamManager teamManager;

    public TeamsCommand(@NotNull TeamManager manager) {
        this.teamManager = manager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, @NotNull String[] args) {
        if(args.length > 1) {
            String teamname = args[1];
            switch(args[0].toLowerCase(Locale.ROOT)) {
                case "add":
                    if(commandSender.hasPermission("varo.team.add")) {
                        // create team
                        teamManager.createTeam(commandSender, teamname);
                    } else commandSender.sendMessage(ChatColor.RED + "Du hast nicht die nötige Berechtigung, diesen Befehl auszuführen");
                    return false;
                case "modify":
                    if(args.length > 4 && args[2].equalsIgnoreCase("displayname")) {
                        if(commandSender.hasPermission("varo.teams.modify.displayname")) {
                            // set displayname
                            teamManager.setDisplayName(commandSender, teamname, args[3]);
                        } else commandSender.sendMessage(ChatColor.RED + "Du hast nicht die nötige Berechtigung, diesen Befehl auszuführen");
                    }
                    break;
                case "members":
                    if(args.length > 3) {
                        if(args[2].equalsIgnoreCase("add")) {
                            if(commandSender.hasPermission("varo.teams.members.add")) {
                                // add member to team
                                teamManager.addMember(commandSender, teamname, args[3]);
                            } else commandSender.sendMessage(ChatColor.RED + "Du hast nicht die nötige Berechtigung, diesen Befehl auszuführen");
                        } else if(args[2].equalsIgnoreCase("remove")) {
                            if(commandSender.hasPermission("varo.teams.members.remove")) {
                                teamManager.removeMember(commandSender, teamname, args[3]);
                            }
                        }
                    }
                    break;
            }
        }

        sendHelp(commandSender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }

    private void sendHelp(CommandSender sender) {

    }
}
