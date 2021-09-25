package de.rytrox.varo.scoreboard;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.teams.events.PlayerTeamJoinEvent;
import de.rytrox.varo.teams.events.PlayerTeamKickEvent;
import de.rytrox.varo.teams.events.TeamMemberSpawnEvent;
import de.rytrox.varo.teams.events.TeamModifyEvent;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScoreBoardManager implements Listener {

    private final Tablist tablist = Tablist.getInstance();
    private final boolean friendlyFire;

    public ScoreBoardManager(@NotNull Varo main) {
        this.friendlyFire = main.getConfig().getBoolean("teams.friendly-fire", false);

        Bukkit.getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void onTeamMemberJoin(@NotNull TeamMemberSpawnEvent event) {
        TeamMember member = event.getMember();

        // cache member
        List<PacketPlayOutScoreboardTeam> teamPacket = tablist.addTeamMember(member, friendlyFire);

        // send all players except the new one the team set packet
        Bukkit.getOnlinePlayers().stream()
                .filter((player) -> !player.equals(event.getPlayer()))
                .forEach((player) ->
                        teamPacket.forEach((packet) -> sendPacket(player, packet))
                );

        // send the new player the new scoreboard
        sendScoreboard(event.getPlayer());
    }

    @EventHandler
    public void onModifyTeam(TeamModifyEvent event) {
        PacketPlayOutScoreboardTeam teamPacket = tablist.updateTeam(event.getTeam());

        if(teamPacket != null) {
            Bukkit.getOnlinePlayers().forEach((player) -> sendPacket(player, teamPacket));
        }
    }

    @EventHandler
    public void onJoinTeam(PlayerTeamJoinEvent event) {
        Player player = event.getPlayer().getPlayer();
        if(!event.isCancelled() && player != null) {
            List<PacketPlayOutScoreboardTeam> teamPacket = tablist.addTeamMember(event.getPlayer(), event.getTeam(), friendlyFire);

            Bukkit.getOnlinePlayers().forEach((p) ->
                    teamPacket.forEach((packet) -> sendPacket(p, packet)));
        }
    }

    @EventHandler
    public void onLeaveTeam(PlayerTeamKickEvent event) {
        Player player = event.getPlayer().getPlayer();
        if(!event.isCancelled() && player != null) {
            // Status 1 means delete a new team, Status 4 means remove a player from scoreboard team (clientside)
            PacketPlayOutScoreboardTeam teamPacket = tablist.removeTeamMember(event.getPlayer());

            if(teamPacket != null) {
                Bukkit.getOnlinePlayers().forEach((p) -> sendPacket(p, teamPacket));
            }
        }
    }

    public void sendScoreboard(@NotNull Player player) {
        tablist.getTeams().forEach((team) -> {
            PacketPlayOutScoreboardTeam teamPacket = new PacketPlayOutScoreboardTeam(team, 0);

            sendPacket(player, teamPacket);
        });
    }

    private void sendPacket(@NotNull Player player, @NotNull Packet<?> packet) {
        System.out.println(player.getName() + " " + packet.getClass().getName());
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
