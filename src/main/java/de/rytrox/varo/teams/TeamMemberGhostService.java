package de.rytrox.varo.teams;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.Team;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.database.enums.PlayerStatus;
import de.rytrox.varo.teams.events.TeamMemberDisconnectEvent;
import de.rytrox.varo.teams.events.TeamMemberSpawnEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class TeamMemberGhostService implements Listener {

    private final Varo main;

    public TeamMemberGhostService(@NotNull Varo main) {
        this.main = main;
    }

    @EventHandler
    public void onTeamMemberSpawn(TeamMemberSpawnEvent event) {
        TeamMember member = event.getMember();
        Player player = member.getPlayer();

        if(member.getStatus() == PlayerStatus.DEAD && player != null) {
            Bukkit.getScheduler().runTask(main, () -> {
                if(member.getTeam() != null) {
                    System.out.println(member.getTeam().getMembers());
                    player.setGameMode(GameMode.SPECTATOR);
                    List<TeamMember> alivePartner = findAliveTeamMember(member.getTeam(), false);

                    if(!alivePartner.isEmpty()) {
                        // get online member
                        Optional<TeamMember> onlinePartner = alivePartner.stream()
                                .filter((other) -> other.getPlayer() != null)
                                .findAny();

                        if(onlinePartner.isPresent()) {
                            Player target = onlinePartner.get().getPlayer();

                            player.setSpectatorTarget(target);
                        } else player.kickPlayer(ChatColor.translateAlternateColorCodes('&', "&8[&6Varo&8] &cDu kannst nur zuschauen, wenn dein Team online ist."));
                    } else player.kickPlayer(ChatColor.translateAlternateColorCodes('&', "&8[&6Varo&8] &cDein Team ist ausgeschieden"));
                } else player.kickPlayer(ChatColor.translateAlternateColorCodes('&', "&8[&6Varo&8] &cTote Spieler ohne Team sind direkt ausgeschieden"));

            });
        }
    }

    @EventHandler
    public void onTeamMemberDisconnect(TeamMemberDisconnectEvent event) {
        TeamMember member = event.getMember();

        Team team = member.getTeam();
        if(team != null && member.getStatus() == PlayerStatus.ALIVE) {
            List<TeamMember> teamMembers = findAliveTeamMember(team, true);
            teamMembers.remove(member);

            List<TeamMember> spectators = team.getMembers().stream()
                    .filter((teamMember) -> teamMember.getStatus() == PlayerStatus.DEAD &&
                            teamMember.getPlayer() != null && teamMember.getPlayer().getSpectatorTarget() == null)
                    .collect(Collectors.toList());

            // set all spectating players to the next member when one member disconnects
            if(teamMembers.isEmpty()) {
                // kick all spectators
                spectators.forEach((spectator) ->
                        Objects.requireNonNull(spectator.getPlayer()).kickPlayer(
                                ChatColor.translateAlternateColorCodes('&', "&8[&6Varo&8] &cDu kannst nur zuschauen, wenn dein Team online ist.")
                        ));
            } else spectators.forEach((spectator) ->
                    Objects.requireNonNull(spectator.getPlayer()).setSpectatorTarget(teamMembers.get(0).getPlayer()));
        }
    }

    @EventHandler
    public void onDenySwitchSpectatorTarget(PlayerTeleportEvent event) {
        if(event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            event.setCancelled(true);
        }
    }

    @NotNull
    private List<TeamMember> findAliveTeamMember(@Nullable Team team, boolean requireOnline) {
        if(team == null)
            return new ArrayList<>();

        return team.getMembers()
                .stream()
                .filter((other) -> other.getStatus() == PlayerStatus.ALIVE)
                .filter((other) -> !requireOnline || other.getPlayer() != null)
                .collect(Collectors.toList());
    }
}
