package de.rytrox.varo.teams;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.Team;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.database.repository.TeamMemberRepository;
import de.rytrox.varo.database.repository.TeamRepository;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class TeamManager implements Listener {

    private final Varo main;

    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;

    public TeamManager(@NotNull Varo main) {
        this.main = main;
        this.teamMemberRepository = new TeamMemberRepository(main.getDB());
        this.teamRepository = new TeamRepository(main.getDB());

        Bukkit.getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void onRegisterPlayerInDatabase(@NotNull PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            // get player
            Player player = event.getPlayer();

            // get TeamMember-Object
            TeamMember member = teamMemberRepository.getPlayer(player);
            if(member == null) {
                // create a new member and save it
                member = new TeamMember();
                member.setTeam(null);
                member.setUniqueID(player.getUniqueId());

                // save entity in Database
                main.getDB().save(member);
                main.getLogger().log(Level.INFO, "Saved Player {0} in database. It's his first start", player.getName());
            }
        });
    }

    /**
     * Creates a Team and inserts it into the Database <br>
     * Attention: This Method runs async
     *
     * @param executor the executor of the creation process
     * @param name the name of the Team
     */
    public void createTeam(CommandSender executor, String name) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            // check if team does not exist...
            if(teamRepository.findByName(name) == null) {
                // create a new Team
                Team team = new Team();
                team.setName(name);

                // and save it in database
                main.getDB().save(team);
                main.getLogger().log(Level.INFO, String.format("%s creates a new Team called %s", executor.getName(), name));
                executor.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', String.format("&7Das Team &a%s &7wurde &aerfolgreich erstellt!", name))
                );
            } else executor.sendMessage(
                    ChatColor.translateAlternateColorCodes('&', "&cDieses Team existiert bereits"));
        });
    }

    /**
     * Sets the displayname of a team and save it asynchronously
     *
     * @param commandSender the executor of the modification
     * @param teamname the name of the team
     * @param teamDisplayName the Displayname of the team (ColorCodes in '&')
     */
    public void setDisplayName(CommandSender commandSender, String teamname, String teamDisplayName) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            // check if team does not exist...
            Team team = teamRepository.findByName(teamname);
            if(team != null) {
                team.setDisplayName(ChatColor.translateAlternateColorCodes('&', teamDisplayName));

                // save it in database
                main.getDB().save(team);
                main.getLogger().log(Level.INFO, String.format("%s modfied displayname of %s to %s",
                        commandSender.getName(), team.getName(), team.getDisplayName()));
                commandSender.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', String.format(
                                "&7Der Displayname von &a%s &7wurde geändert zu %s", team.getName(), team.getDisplayName())
                        )
                );
            } else commandSender.sendMessage(
                    ChatColor.translateAlternateColorCodes('&', "&cDieses Team existiert nicht"));
        });
    }

    /**
     * Adds a member to a team
     *
     * @param commandSender the executor of the Process
     * @param teamname the name of the Team
     * @param playerName the name of the player
     */
    public void addMember(CommandSender commandSender, String teamname, String playerName) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            Team team = teamRepository.findByName(teamname);
            if(team != null) {
                TeamMember member = teamMemberRepository.getPlayer(Bukkit.getOfflinePlayer(playerName));
                if(member != null) {
                    if(!team.equals(member.getTeam())) {
                        member.setTeam(team);
                        main.getDB().save(member);
                        main.getLogger().log(Level.INFO, String.format("%s adds %s to Team %s", commandSender.getName(), playerName, team.getName()));
                        commandSender.sendMessage(
                                ChatColor.translateAlternateColorCodes('&', String.format(
                                        "&7Der Spieler &a%s &7wurde zum &5Team &d%s &ahinzugefügt", playerName, team.getName()
                                ))
                        );
                    } else
                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                "&cDieser Spieler ist bereits in diesem Team"));
                } else
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                            "&cDer Spieler %s konnte nicht gefunden werden. Prozess wurde abgebrochen", playerName)));
            } else commandSender.sendMessage(
                    ChatColor.translateAlternateColorCodes('&', "&cDieses Team existiert nicht"));
        });
    }

    public void removeMember(CommandSender executor, String teamname, String playerName) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            Team team = teamRepository.findByName(teamname);
            if(team != null) {
                TeamMember member = teamMemberRepository.getPlayer(Bukkit.getOfflinePlayer(playerName));
                if(member != null) {
                    if(team.equals(member.getTeam())) {
                        member.setTeam(null);
                        main.getDB().save(member);
                        main.getLogger().log(Level.INFO,
                                String.format("%s removes %s of Team %s", executor.getName(), playerName, team.getName()));
                        executor.sendMessage(
                                ChatColor.translateAlternateColorCodes('&', String.format(
                                        "&7Der Spieler &a%s &7wurde vom &5Team &d%s &centfernt", playerName, team.getName()
                                ))
                        );
                    } else
                        executor.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                "&cDieser Spieler ist nicht in diesem Team"));
                } else
                    executor.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                            "&cDer Spieler %s konnte nicht gefunden werden. Prozess wurde abgebrochen", playerName)));
            } else executor.sendMessage(
                    ChatColor.translateAlternateColorCodes('&', "&cDieses Team existiert nicht"));
        });
    }
}
