package de.rytrox.varo.pvp;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.Team;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.database.enums.PlayerStatus;
import de.rytrox.varo.database.repository.TeamMemberRepository;
import de.rytrox.varo.database.repository.TeamRepository;
import de.rytrox.varo.gamestate.GameStateHandler;
import de.rytrox.varo.message.MessageService;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlayerDeathListener implements Listener {

    private final Varo main;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MessageService messageService;

    public PlayerDeathListener(Varo main) {
        this.main = main;
        this.teamRepository = new TeamRepository(main.getDB());
        this.teamMemberRepository = new TeamMemberRepository(main.getDB());
        this.messageService = main.getMessageService();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        Optional<TeamMember> teamMemberOptional = this.teamMemberRepository.findPlayerByUUID(event.getEntity().getUniqueId());

        if(teamMemberOptional.isPresent()) {
            TeamMember member = teamMemberOptional.get();

            // update player status
            member.setStatus(PlayerStatus.DEAD);
            main.getDB().update(member);

            // announce death
            messageService.writeMessage(
                    ChatColor.translateAlternateColorCodes('&',
                        String.format("&eDer Spieler &4%s &e aus dem Team %s &eist gestorben!",
                            event.getEntity().getName(),
                            member.getTeam().getDisplayName())),
                    MessageService.DiscordColor.YELLOW,
                    true);

            // check if team has members left
            if(member.getTeam().getMembers().stream().noneMatch(m -> m.getStatus() == PlayerStatus.ALIVE)) {
                // announce elimination
                messageService.writeMessage(
                        ChatColor.translateAlternateColorCodes('&',
                                String.format("&4Das Team %s &4ist ausgeschieden!",
                                        member.getTeam().getDisplayName())),
                        MessageService.DiscordColor.RED,
                        true
                );

                // check if final or game over
                List<Team> runningTeams = teamRepository.getAllTeams().stream().filter(team -> team.getMembers().stream().anyMatch(m -> m.getStatus() == PlayerStatus.ALIVE)).collect(Collectors.toList());

                // check for final
                if(runningTeams.size() == 2) {
                    main.getGameStateHandler().setCurrentGameState(GameStateHandler.GameState.FINAL);

                    // announce final
                    messageService.writeMessage(
                            ChatColor.translateAlternateColorCodes('&',
                                    String.format("&6Das Finale zwischen Team %s &6und Team %s &6hat begonnen!",
                                            runningTeams.get(0).getDisplayName(),
                                            runningTeams.get(1).getDisplayName())),
                            MessageService.DiscordColor.YELLOW,
                            true
                    );

                // check for game end
                } else if(runningTeams.size() == 1) {
                    main.getGameStateHandler().setCurrentGameState(GameStateHandler.GameState.POST);

                    // announce game end and winner
                    messageService.writeMessage(
                            ChatColor.translateAlternateColorCodes('&',
                                    String.format("&aDas Finale ist vorbei!!! Das Team %s &ahat das Spiel gewonnen!",
                                            runningTeams.get(0).getDisplayName())),
                            MessageService.DiscordColor.YELLOW,
                            true
                    );
                }
            }
        }

    }

}
