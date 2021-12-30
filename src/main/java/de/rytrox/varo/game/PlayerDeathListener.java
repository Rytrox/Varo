package de.rytrox.varo.game;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.Team;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.database.enums.PlayerStatus;
import de.rytrox.varo.database.repository.TeamMemberRepository;
import de.rytrox.varo.database.repository.TeamRepository;
import de.rytrox.varo.gamestate.GameStateHandler;
import de.rytrox.varo.message.MessageService;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.List;
import java.util.Optional;

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
    public void onRespawn(PlayerRespawnEvent event) {
        Optional<TeamMember> teamMemberOptional = this.teamMemberRepository.findPlayerByUUID(event.getPlayer().getUniqueId());

        if(teamMemberOptional.isPresent()
                && teamMemberOptional.get().getSpawnPoint() != null) {
            event.setRespawnLocation(teamMemberOptional.get().getSpawnPoint().getLocation());
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        // remove default death message
        event.setDeathMessage(null);

        GameStateHandler.GameState gameState = main.getGameStateHandler().getCurrentGameState();

        if(gameState != GameStateHandler.GameState.MAIN && gameState != GameStateHandler.GameState.FINAL) {
            return;
        }

        Optional<TeamMember> teamMemberOptional = this.teamMemberRepository.findPlayerByUUID(event.getEntity().getUniqueId());

        if(teamMemberOptional.isPresent()) {
            TeamMember member = teamMemberOptional.get();

            // update player status
            member.setStatus(PlayerStatus.DEAD);
            main.getDB().update(member);

            // kick player
            event.getEntity().kickPlayer(ChatColor.translateAlternateColorCodes('&',
                    "&cDu bist ausgeschieden!"));

            // check if there is a killer
            Player killer = event.getEntity().getKiller();

            boolean validKiller = false;
            if(killer != null) {
                Optional<TeamMember> killerMember = this.teamMemberRepository.findPlayerByUUID(event.getEntity().getKiller().getUniqueId());

                if(killerMember.isPresent()) {

                    messageService.writeMessage(
                            String.format("&eDer Spieler &4%s &eaus dem Team %s &ewurde vom Spieler &c%s &eaus dem Team %s &egetötet!",
                                event.getEntity().getName(),
                                member.getTeam().getDisplayName(),
                                event.getEntity().getKiller().getName(),
                                killerMember.get().getTeam().getDisplayName()),
                            MessageService.DiscordColor.YELLOW,
                            true);

                    validKiller = true;
                }
            }

            if(!validKiller) {
                // announce death
                messageService.writeMessage(
                        String.format("&eDer Spieler &4%s &e aus dem Team %s &eist gestorben!",
                            event.getEntity().getName(),
                            member.getTeam().getDisplayName()),
                        MessageService.DiscordColor.YELLOW,
                        true);
            }

            // check if team has members left
            if(member.getTeam().getMembers().stream().noneMatch(m -> m.getStatus() == PlayerStatus.ALIVE)) {
                // announce elimination
                messageService.writeMessage(
                        String.format("&4Das Team %s &4ist ausgeschieden!",
                            member.getTeam().getDisplayName()),
                        MessageService.DiscordColor.RED,
                        true
                );

                // check if final or game over
                List<Team> runningTeams = teamRepository.getAllTeamsWithAliveMembers();

                // check for final
                if(runningTeams.size() == 2) {
                    main.getGameStateHandler().setCurrentGameState(GameStateHandler.GameState.FINAL);

                    // announce final
                    messageService.writeMessage(
                            String.format("&6Das Finale zwischen Team %s &6und Team %s &6hat begonnen!",
                                runningTeams.get(0).getDisplayName(),
                                runningTeams.get(1).getDisplayName()),
                            MessageService.DiscordColor.YELLOW,
                            true
                    );

                // check for game end
                } else if(runningTeams.size() == 1) {
                    main.getGameStateHandler().setCurrentGameState(GameStateHandler.GameState.POST);

                    // announce game end and winner
                    messageService.writeMessage(
                            String.format("&aDas Finale ist vorbei!!! Das Team %s &ahat das Spiel gewonnen!",
                                runningTeams.get(0).getDisplayName()),
                            MessageService.DiscordColor.YELLOW,
                            true
                    );
                }
            }
        }

    }

}
