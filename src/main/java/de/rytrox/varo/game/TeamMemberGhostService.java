package de.rytrox.varo.game;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.Team;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.database.enums.PlayerStatus;
import de.rytrox.varo.teams.events.TeamMemberDisconnectEvent;
import de.rytrox.varo.teams.events.TeamMemberJoinEvent;
import de.rytrox.varo.teams.events.TeamMemberLoginEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class TeamMemberGhostService implements Listener {

    private final Map<Player, Entity> spectatorTarget = new HashMap<>();

    private final Varo main;

    public TeamMemberGhostService(@NotNull Varo main) {
        this.main = main;
    }

    @EventHandler
    public void onDenyGhosting(TeamMemberLoginEvent event) {
        TeamMember member = event.getMember();
        if(member.getStatus() == PlayerStatus.DEAD) {
            if(member.getTeam() != null) {
                List<TeamMember> alivePartner = findAliveTeamMember(member.getTeam(), false);

                if(!alivePartner.isEmpty()) {
                    // get online member
                    Optional<TeamMember> onlinePartner = alivePartner.stream()
                            .filter((other) -> other.getPlayer() != null)
                            .findAny();

                    if(onlinePartner.isPresent()) {
                        Player target = onlinePartner.get().getPlayer();

                        spectatorTarget.put(event.getPlayer(), target);
                    } else {
                        event.setCancelled(true);
                        event.setCancelMessage(ChatColor.translateAlternateColorCodes('&', "&8[&6Varo&8] &cDu kannst nur zuschauen, wenn dein Team online ist."));
                    }
                } else {
                    event.setCancelled(true);
                    event.setCancelMessage(ChatColor.translateAlternateColorCodes('&', "&8[&6Varo&8] &cDein Team ist ausgeschieden"));
                }
            } else {
                event.setCancelled(true);
                event.setCancelMessage(ChatColor.translateAlternateColorCodes('&', "&8[&6Varo&8] &cTote Spieler ohne Team sind direkt ausgeschieden"));
            }
        }
    }

    @EventHandler
    public void onTeamMemberSpawn(TeamMemberJoinEvent event) {
        Entity target = spectatorTarget.get(event.getPlayer());

        if(target != null) {
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
            Bukkit.getScheduler().runTaskLater(main, () -> event.getPlayer().setSpectatorTarget(target), 10L);
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
                Bukkit.getScheduler().runTask(main, () -> {
                    spectators.forEach((spectator) ->
                            Objects.requireNonNull(spectator.getPlayer()).kickPlayer(
                                    ChatColor.translateAlternateColorCodes('&', "&8[&6Varo&8] &cDu kannst nur zuschauen, wenn dein Team online ist.")
                            ));
                });
            } else spectators.forEach((spectator) ->
                    Objects.requireNonNull(spectator.getPlayer()).setSpectatorTarget(teamMembers.get(0).getPlayer()));
        }
    }

    @EventHandler
    public void onDenySwitchSpectatorTarget(PlayerTeleportEvent event) {
        if(event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            event.setCancelled(true);

            event.getPlayer().setSpectatorTarget(spectatorTarget.get(event.getPlayer()));
        }
    }

    @EventHandler
    public void onCancelQuit(PlayerToggleSneakEvent event) {
        if(event.getPlayer().getGameMode() == GameMode.SPECTATOR && event.getPlayer().getSpectatorTarget() != null) {
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
