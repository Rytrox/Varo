package de.rytrox.varo.teams;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

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
                    break;
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
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, @NotNull String[] args) {
        List<String> predictions = new ArrayList<>();

        switch (args.length) {
            case 1:
                StringUtil.copyPartialMatches(args[0], Arrays.asList("add", "modify", "members"), predictions);
                break;
            case 2:
                if(!args[0].equalsIgnoreCase("add")) {
                    StringUtil.copyPartialMatches(args[1], teamManager.getTeamNames(), predictions);
                }
                break;
            case 3:
                if(args[0].equalsIgnoreCase("members")) {
                    StringUtil.copyPartialMatches(args[2], Arrays.asList("add", "remove"), predictions);
                } else if(args[0].equalsIgnoreCase("modify")) {
                    StringUtil.copyPartialMatches(args[2], Collections.singletonList("displayname"), predictions);
                }
                break;
            case 4:
                if(args[0].equalsIgnoreCase("members")) {
                    StringUtil.copyPartialMatches(args[3], Bukkit.getOnlinePlayers()
                            .stream()
                            .map(Player::getName)
                            .collect(Collectors.toList()), predictions);
                }
                break;
        }

        return predictions;
    }

    private void sendHelp(@NotNull CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/teams&8: &7Ruft die Hilfe auf"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/teams add <name>&8: &7Erstellt ein neues &5Team &7mit internem Namen <name>"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/teams modify <name> displayname <displayname>&8: &7Setzt den Namen des &5Teams, der angezeigt wird. Farbcodes werden mit '&' geschrieben"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/teams members <name> add <playername>&8: &7Fügt einen Spieler zu einem &5Team hinzu. Der Spieler muss aber bereits auf dem Server gewesen sein"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a/teams members <name> remove <playername>&8: &7Entfernt einen Spieler aus einem &5Team"));
    }
}
