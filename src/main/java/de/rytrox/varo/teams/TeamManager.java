package de.rytrox.varo.teams;

import de.rytrox.varo.Varo;
import de.rytrox.varo.message.chatlog.ChatLogType;
import de.rytrox.varo.database.entity.ChatLog;
import de.rytrox.varo.database.entity.Team;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.database.repository.TeamMemberRepository;
import de.rytrox.varo.database.repository.TeamRepository;

import de.rytrox.varo.teams.scoreboard.Tablist;
import de.rytrox.varo.teams.events.*;
import de.rytrox.varo.teams.inventory.TeamInventoryManager;
import de.rytrox.varo.teams.spawnpoint.SpawnpointCommand;

import de.rytrox.varo.utils.MojangAPI;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class TeamManager implements Listener {

    private final Varo main;

    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;

    private final int maxPlayersPerTeam;

    public TeamManager(@NotNull Varo main) {
        this.main = main;
        this.teamMemberRepository = new TeamMemberRepository(main.getDB());
        this.teamRepository = new TeamRepository(main.getDB());

        this.maxPlayersPerTeam = main.getConfig().getInt("teams.maxMembers", 2);

        Bukkit.getPluginManager().registerEvents(this, main);
        Bukkit.getPluginManager().registerEvents(new TeamEventManager(main), main);
        Bukkit.getPluginManager().registerEvents(new TeamInventoryManager(main, teamMemberRepository), main);
        Bukkit.getPluginManager().registerEvents(new TeamMemberGhostService(main), main);

        main.getCommand("teams").setExecutor(new TeamsCommand(this));
        main.getCommand("spawnpoint").setExecutor(new SpawnpointCommand(main, this.teamMemberRepository));
    }

    @EventHandler
    public void onDisconnectCacheClear(@NotNull PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            TeamMember member = teamMemberRepository.getPlayer(event.getPlayer());

            if(member != null) {
                Bukkit.getPluginManager().callEvent(new TeamMemberDisconnectEvent(event.getPlayer(), member));
            }
        });
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        main.getChatLogRepository().addChatLog(new ChatLog(event.getPlayer().getName(), ChatLogType.GLOBAL.getName(), event.getMessage()));
        event.setFormat(Tablist.getInstance().getPrefix(event.getPlayer()) + ChatColor.translateAlternateColorCodes('&', "%s &8» &7%s"));
    }

    /**
     * Returns a List of all Teamnames
     *
     * @return a list containing all Team names
     */
    @NotNull
    public List<String> getTeamNames() {
        return this.teamRepository.getAllTeamNames();
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
            if(!teamRepository.findByName(name).isPresent()) {
                // create a new Team
                Team team = new Team();
                team.setName(name);

                TeamCreateEvent event = new TeamCreateEvent(team);
                Bukkit.getPluginManager().callEvent(event);
                if(!event.isCancelled()) {
                    // and save it in database
                    main.getDB().save(team);
                    main.getLogger().log(Level.INFO, String.format("%s creates a new Team called %s", executor.getName(), name));
                    executor.sendMessage(
                            ChatColor.translateAlternateColorCodes('&', String.format("&7Das &5Team &d%s &7wurde &aerfolgreich erstellt!", name))
                    );
                }
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
            Optional<Team> teamOptional = teamRepository.findByName(teamname);
            if(teamOptional.isPresent()) {
                Team team = teamOptional.get();

                team.setDisplayName(teamDisplayName);

                TeamModifyEvent event = new TeamModifyEvent(team);
                Bukkit.getPluginManager().callEvent(event);
                if(!event.isCancelled()) {
                    // save it in database
                    main.getDB().save(team);
                    main.getLogger().log(Level.INFO, String.format("%s modfied displayname of %s to %s",
                            commandSender.getName(), team.getName(), team.getDisplayName()));
                    commandSender.sendMessage(
                            ChatColor.translateAlternateColorCodes('&', String.format(
                                    "&7Der Displayname von &d%s &7wurde geändert zu %s", team.getName(), team.getDisplayName())
                            )
                    );
                }
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
            Optional<Team> teamOptional = teamRepository.findByName(teamname);
            if(teamOptional.isPresent()) {

                Team team = teamOptional.get();
                MojangAPI.getOfflinePlayer(playerName)
                        .thenApply((player) -> {
                            TeamMember member = teamMemberRepository.getPlayer());
                            if(member != null) {
                                if(!team.equals(member.getTeam())) {
                                    if(team.getMembers().size() < this.maxPlayersPerTeam) {
                                        PlayerTeamJoinEvent event = new PlayerTeamJoinEvent(team, member);
                                        Bukkit.getPluginManager().callEvent(event);

                                        member.setTeam(team);
                                        if(!event.isCancelled()) {
                                            main.getDB().save(member);
                                            main.getLogger().log(Level.INFO, String.format("%s adds %s to Team %s", commandSender.getName(), playerName, team.getName()));
                                            commandSender.sendMessage(
                                                    ChatColor.translateAlternateColorCodes('&', String.format(
                                                            "&7Der Spieler &a%s &7wurde zum &5Team &d%s &ahinzugefügt", playerName, team.getName()
                                                    ))
                                            );
                                        }
                                    } else
                                        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                                "&cDieses Team ist bereits voll!"));
                                } else
                                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                            "&cDieser Spieler ist bereits in diesem Team"));
                        });

                } else
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                            "&cDer Spieler %s konnte nicht gefunden werden. Prozess wurde abgebrochen", playerName)));
            } else commandSender.sendMessage(
                    ChatColor.translateAlternateColorCodes('&', "&cDieses Team existiert nicht"));
        });
    }

    public void removeMember(CommandSender executor, String teamname, String playerName) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            Optional<Team> teamOptional = teamRepository.findByName(teamname);
            if(teamOptional.isPresent()) {

                Team team = teamOptional.get();
                TeamMember member = teamMemberRepository.getPlayer(Bukkit.getOfflinePlayer(playerName));
                if(member != null) {
                    if(team.equals(member.getTeam())) {
                        PlayerTeamKickEvent event = new PlayerTeamKickEvent(team, member);
                        Bukkit.getPluginManager().callEvent(event);

                        member.setTeam(null);
                        if(!event.isCancelled()) {
                            main.getDB().save(member);
                            main.getLogger().log(Level.INFO,
                                    String.format("%s removes %s of Team %s", executor.getName(), playerName, team.getName()));
                            executor.sendMessage(
                                    ChatColor.translateAlternateColorCodes('&', String.format(
                                            "&7Der Spieler &a%s &7wurde vom &5Team &d%s &centfernt", playerName, team.getName()
                                    ))
                            );
                        }
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

    public void setPrefix(CommandSender commandSender, String teamname, String prefix) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            // check if team does not exist...
            Optional<Team> teamOptional = teamRepository.findByName(teamname);
            if(teamOptional.isPresent()) {

                Team team = teamOptional.get();
                if(prefix.length() < 8) {
                    team.setPrefix(ChatColor.translateAlternateColorCodes('&',
                            String.format("&8[%s&8]&7 ", prefix))
                    );

                    TeamModifyEvent event = new TeamModifyEvent(team);
                    Bukkit.getPluginManager().callEvent(event);
                    if(!event.isCancelled()) {
                        // save it in database
                        main.getDB().save(team);
                        main.getLogger().log(Level.INFO, String.format("%s modfied prefix of %s to %s",
                                commandSender.getName(), team.getName(), team.getPrefix()));
                        commandSender.sendMessage(
                                ChatColor.translateAlternateColorCodes('&', String.format(
                                        "&7Der Prefix von &5Team &d%s &7wurde geändert zu %s", team.getName(), team.getPrefix())
                                )
                        );
                    }
                } else commandSender.sendMessage(ChatColor.RED + "Der Prefix ist zu lang und kann nicht gespeichert werden");
            } else commandSender.sendMessage(
                    ChatColor.translateAlternateColorCodes('&', "&cDieses Team existiert nicht"));
        });
    }
}
