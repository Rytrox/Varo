package de.rytrox.varo.scoreboard;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.teams.events.TeamMemberSpawnEvent;
import de.rytrox.varo.teams.events.TeamModifyEvent;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class ScoreBoardManager implements Listener {

    private final boolean friendlyFire;

    public ScoreBoardManager(@NotNull Varo main) {
        this.friendlyFire = main.getConfig().getBoolean("teams.friendly-fire", false);

        Bukkit.getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void onTeamMemberJoin(@NotNull TeamMemberSpawnEvent event) throws IllegalAccessException {
        TeamMember member = event.getMember();

        // cache member
        if(member.getTeam() != null) {
            // Status 0 means create a new team, Status 3 means add a new player to scoreboard team (clientside)
            int status = Tablist.getInstance().registerTeam(member.getTeam()) ? 0 : 3;

            ScoreboardTeam team = Tablist.getInstance().getScoreboardTeam(member.getTeam());
            team.setAllowFriendlyFire(friendlyFire);

            PacketPlayOutScoreboardTeam teamPacket = new PacketPlayOutScoreboardTeam(team, status);

            // send all players except the new one the team set packet
            Bukkit.getOnlinePlayers().stream()
                    .filter((player) -> !player.equals(event.getPlayer()))
                    .forEach((player) -> sendPacket(player, teamPacket));
        }

        // send the new player the new scoreboard
        sendScoreboard(event.getPlayer());
    }

    @EventHandler
    public void onModifyTeam(TeamModifyEvent event) {
        ScoreboardTeam team = Tablist.getInstance().getScoreboardTeam(event.getTeam());
        team.setAllowFriendlyFire(friendlyFire);

        PacketPlayOutScoreboardTeam teamPacket = new PacketPlayOutScoreboardTeam(team, 2);
        Bukkit.getOnlinePlayers().forEach((player) -> sendPacket(player, teamPacket));
    }

    public void sendScoreboard(@NotNull Player player) {
        Tablist.getInstance().getRegisteredTeams().forEach((team) -> {
            PacketPlayOutScoreboardTeam teamPacket = new PacketPlayOutScoreboardTeam(
                    Tablist.getInstance().getScoreboardTeam(team), 0);
            sendPacket(player, teamPacket);
        });
    }

    private void sendPacket(@NotNull Player player, @NotNull Packet<?> packet) {
        System.out.println(player.getName() + " " + packet.getClass().getName());
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
