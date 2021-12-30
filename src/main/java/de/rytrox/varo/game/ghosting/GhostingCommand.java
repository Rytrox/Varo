package de.rytrox.varo.game.ghosting;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.database.enums.PlayerStatus;
import de.rytrox.varo.database.repository.TeamMemberRepository;
import de.rytrox.varo.utils.CommandHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class GhostingCommand implements TabExecutor {

    private final TeamMemberRepository teamMemberRepository;

    public GhostingCommand(Varo main) {
        this.teamMemberRepository = new TeamMemberRepository(main.getDB());
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;

            Optional<TeamMember> teamMember = teamMemberRepository.findPlayerByUUID(player.getUniqueId());

            if(teamMember.isPresent()) {

                // check if player is dead
                if(teamMember.get().getStatus() == PlayerStatus.DEAD) {
                    if(args.length == 1) {

                        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[0]);
                        Optional<TeamMember> targetMember = teamMemberRepository.findPlayer(targetPlayer);

                        if(targetMember.isPresent()) {
                            if(targetMember.get().getTeam() != null
                                    && targetMember.get().getTeam().equals(teamMember.get().getTeam())) {
                                if(targetPlayer.isOnline()) {
                                    if(targetMember.get().getStatus() == PlayerStatus.ALIVE) {
                                        player.setSpectatorTarget(targetPlayer.getPlayer());
                                    } else {
                                        player.sendMessage(ChatColor.RED + "Dein angegebenes Teammitglied ist nicht mehr am Leben");
                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "Dein angegebenes Teammitglied ist gerade nicht online");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "Du kannst nur deine Teammates ghosten");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "Der angegebene Spieler ist kein Teilnehmer");
                        }
                    } else {
                        sendHelp(player);
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Nur ausgeschiedene Spieler können ihre Teammitglieder ghosten");
                }

            } else {
                player.sendMessage(ChatColor.RED + "Nur registrierte Teilnehmer können diesen Befehl nutzen");
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "Dieser Befehl kann nur von Spielern ausgeführt werden!");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;

            Optional<TeamMember> teamMember = teamMemberRepository.findPlayerByUUID(player.getUniqueId());

            if (teamMember.isPresent()) {
                return Objects.requireNonNull(teamMember
                                .get()
                                .getTeam())
                        .getMembers()
                        .stream()
                        .filter(member -> member.getStatus() == PlayerStatus.ALIVE && member.getOfflinePlayer().isOnline())
                        .map(member -> member.getOfflinePlayer().getPlayer().getName())
                        .collect(Collectors.toList());
            }

        }
        return new ArrayList<>();
    }

    private void sendHelp(Player sender) {
        CommandHelper.sendCommandHeader("/ghosting", sender);
        CommandHelper.sendCommandExplanation("/ghosting <teammate>", "Wechselt die Ansicht auf das angegebene Teammitglied", sender);
    }

}
