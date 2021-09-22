package de.rytrox.varo.scoreboard;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.Team;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.teams.events.TeamMemberDisconnectEvent;
import de.rytrox.varo.teams.events.TeamMemberSpawnEvent;
import de.rytrox.varo.teams.events.TeamModifyEvent;
import net.minecraft.server.v1_8_R3.*;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ScoreBoardManager implements Listener {

    private final Set<Team> onlineTeams = new HashSet<>();

    private final boolean friendlyFire;
    private final Scoreboard tablist;

    public ScoreBoardManager(@NotNull Varo main) {
        this.friendlyFire = main.getConfig().getBoolean("teams.friendly-fire", false);

        this.tablist = new Scoreboard();

        Bukkit.getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void onTeamMemberJoin(@NotNull TeamMemberSpawnEvent event) throws IllegalAccessException {
        TeamMember member = event.getMember();

        // cache member
        if(member.getTeam() != null) {
            PacketPlayOutScoreboardTeam teamPacket;
            if(onlineTeams.add(member.getTeam())) {
                // create a new team for this scoreboard
                ScoreboardTeam team = tablist.createTeam(member.getTeam().getName());

                team.setAllowFriendlyFire(this.friendlyFire);
                team.setPrefix(Optional.ofNullable(member.getTeam().getPrefix()).orElse(""));
                team.setDisplayName(Optional.ofNullable(member.getTeam().getDisplayName()).orElse(""));

                // add the new player to the scoreboard
                member.getTeam().getMembers().forEach((m) ->
                        tablist.addPlayerToTeam(Bukkit.getOfflinePlayer(m.getUniqueID()).getName(), member.getTeam().getName()));

                teamPacket = new PacketPlayOutScoreboardTeam(team, 0);
            } else {
                // add player to existing team
                ScoreboardTeam team = tablist.getTeam(member.getTeam().getName());
                tablist.addPlayerToTeam(event.getPlayer().getName(), team.getName());

                teamPacket = new PacketPlayOutScoreboardTeam(team, 0);
            }

            // send all players except the new one the team set packet
            Bukkit.getOnlinePlayers().stream()
                    .filter((player) -> !player.equals(event.getPlayer()))
                    .forEach((player) -> sendPacket(player, teamPacket));
        }

        // send the new player the new scoreboard
        sendScoreboard(event.getPlayer());
    }

    @EventHandler
    public void onTeamMemberDisconnect(TeamMemberDisconnectEvent event) {
        TeamMember member = event.getMember();

        if(member.getTeam() != null) {
            Team team = member.getTeam();

            if(team.getMembers().stream()
                    .noneMatch((m) -> Bukkit.getPlayer(m.getUniqueID()) != null)) {
                onlineTeams.remove(team);
            }
        }
    }

    @EventHandler
    public void onModifyTeam(TeamModifyEvent event) {
        Team team = event.getTeam();
        ScoreboardTeam scoreboardTeam = tablist.getTeam(team.getName());

        scoreboardTeam.setPrefix(Optional.ofNullable(team.getPrefix()).orElse(""));
        scoreboardTeam.setDisplayName(Optional.ofNullable(team.getDisplayName()).orElse(""));

        PacketPlayOutScoreboardTeam teamPacket = new PacketPlayOutScoreboardTeam(scoreboardTeam, 2);
        Bukkit.getOnlinePlayers().forEach((player) -> sendPacket(player, teamPacket));
    }

    public void sendScoreboard(@NotNull Player player) {
        this.onlineTeams.forEach((team) -> {
            ScoreboardTeam scoreboardTeam = tablist.getTeam(team.getName());

            PacketPlayOutScoreboardTeam teamPacket = new PacketPlayOutScoreboardTeam(scoreboardTeam, 0);
            sendPacket(player, teamPacket);
        });
    }

    private void sendPacket(@NotNull Player player, @NotNull Packet<?> packet) {
        System.out.println(player.getName() + " " + packet.getClass().getName());
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
