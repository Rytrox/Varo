package de.rytrox.varo.teams.scoreboard;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.database.repository.TeamMemberRepository;
import de.rytrox.varo.teams.events.*;
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

        // add all teammembers to scoreboard and send scoreboard
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            new TeamMemberRepository(main.getDB())
                    .getOnlineMembers()
                    .forEach(member -> Tablist.getInstance().addTeamMember(member, friendlyFire));

            Bukkit.getOnlinePlayers().forEach(this::sendScoreboard);
        });
    }

    @EventHandler
    public void onTeamMemberJoin(@NotNull TeamMemberJoinEvent event) {
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

        if (teamPacket != null) {
            Bukkit.getOnlinePlayers().forEach((player) -> sendPacket(player, teamPacket));
        }
    }

    @EventHandler
    public void onJoinTeam(PlayerTeamJoinEvent event) {
        Player player = event.getPlayer().getPlayer();
        if (!event.isCancelled() && player != null) {
            List<PacketPlayOutScoreboardTeam> teamPacket = tablist.addTeamMember(event.getPlayer(), event.getTeam(), friendlyFire);

            Bukkit.getOnlinePlayers().forEach((p) ->
                    teamPacket.forEach((packet) -> sendPacket(p, packet)));
        }
    }

    @EventHandler
    public void onLeaveTeam(PlayerTeamKickEvent event) {
        Player player = event.getPlayer().getPlayer();
        if (!event.isCancelled() && player != null) {
            // Status 1 means delete a new team, Status 4 means remove a player from scoreboard team (clientside)
            PacketPlayOutScoreboardTeam teamPacket = tablist.removeTeamMember(event.getPlayer());

            if (teamPacket != null) {
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

    @NotNull
    public String getTablistName(@NotNull Player player) {
        return tablist.getPrefix(player) + player.getName();
    }

    @NotNull
    public String getChatName(@NotNull Player player) {
        return tablist.getDisplayName(player);
    }

    private void sendPacket(@NotNull Player player, @NotNull Packet<?> packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
