package de.rytrox.varo.scoreboard;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.Team;
import de.rytrox.varo.database.entity.TeamMember;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_8_R3.Scoreboard;
import net.minecraft.server.v1_8_R3.ScoreboardTeam;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ScoreBoardManager {

    private final Varo main;

    private final boolean friendlyFire;

    public ScoreBoardManager(@NotNull Varo main) {
        this.main = main;
        this.friendlyFire = main.getConfig().getBoolean("teams.friendly-fire", false);

        sendScoreboard();
    }

    public void sendScoreboard() {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            Scoreboard scoreboard = buildTabList();

            Bukkit.getOnlinePlayers().forEach((receiver) -> {
                System.out.println("Send Packet to " + receiver.getName());
                // send to client
                scoreboard.getTeams().forEach((team) -> {
                    PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(team, 0);

                    ((CraftPlayer) receiver).getHandle().playerConnection.sendPacket(packet);
                });
            });
        });
    }

    private Scoreboard buildTabList() {
        List<TeamMember> onlineMembers = main.getTeamManager().getOnlineMembers();
        Set<Team> onlineTeams = onlineMembers.stream()
                .map(TeamMember::getTeam)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Scoreboard scoreboard = new Scoreboard();

        // create teams
        onlineTeams.forEach((team) -> {
            ScoreboardTeam scoreboardTeam = scoreboard.createTeam(team.getName());
            scoreboardTeam.setDisplayName(Optional.ofNullable(team.getDisplayName()).orElse(""));
            scoreboardTeam.setPrefix(Optional.ofNullable(team.getPrefix()).orElse(""));
            scoreboardTeam.setAllowFriendlyFire(friendlyFire);

        });

        // set players to team
        onlineMembers.forEach((member) -> {
            Player player = Bukkit.getPlayer(member.getUniqueID());

            if(member.getTeam() != null) {
                scoreboard.addPlayerToTeam(player.getName(), member.getTeam().getName());
            }
        });

        return scoreboard;
    }
}
