package de.rytrox.varo.scoreboard;

import de.rytrox.varo.database.entity.Team;
import de.rytrox.varo.database.entity.TeamMember;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_8_R3.Scoreboard;
import net.minecraft.server.v1_8_R3.ScoreboardTeam;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public final class Tablist extends Scoreboard {

    private static final Tablist INSTANCE = new Tablist();

    public static Tablist getInstance() {
        return INSTANCE;
    }

    @Override
    public Collection<String> getTeamNames() {
        return getTeams()
                .stream()
                .map(ScoreboardTeam::getName)
                .collect(Collectors.toList());
    }

    /**
     * Removes a TeamMember from their current scoreboard-team.
     *
     * @param member the member you want to remove
     * @return The packet that needs to be sent to all clients
     */
    @Nullable
    public PacketPlayOutScoreboardTeam removeTeamMember(@NotNull TeamMember member) {
        // remove team from loaded when no other player in a team is online
        if(member.getTeam() != null) {
            ScoreboardTeam team = this.getTeam(member.getTeam().getName());

            if(team != null) {
                // returns delete packet when no other player is online
                if(member.getTeam().getMembers()
                        .stream()
                        .noneMatch((m) -> m != member && m.getPlayer() != null)) {
                    this.removeTeam(team);

                    return new PacketPlayOutScoreboardTeam(team, 1);
                } else {
                    // remove team member from scoreboard
                    INSTANCE.removePlayerFromTeam(member.getOfflinePlayer().getName(), team);

                    return new PacketPlayOutScoreboardTeam(team,
                            Collections.singletonList(member.getOfflinePlayer().getName()), 4);
                }
            }
        }

        return null;
    }

    /**
     * Adds the member to a scoreboard team and returns the packet that must be sent to the client
     *
     * @param member the member itself
     * @return the packet that must be sent to every client. Can be null
     */
    @NotNull
    public List<PacketPlayOutScoreboardTeam> addTeamMember(@NotNull TeamMember member, boolean friendlyFire) {
        return addTeamMember(member, member.getTeam(), friendlyFire);
    }

    /**
     * Adds the member to a scoreboard team and returns the packet that must be sent to the client
     *
     * @param member the member itself
     * @param team the team where the player is going to be added
     * @return a list of packets that must be sent to every client. Can be null
     */
    @NotNull
    public List<PacketPlayOutScoreboardTeam> addTeamMember(@NotNull TeamMember member, @Nullable Team team, boolean friendlyFire) {
        List<PacketPlayOutScoreboardTeam> list = new ArrayList<>();

        if(team != null) {
            ScoreboardTeam scoreboardTeam = this.getTeam(team.getName());
            PacketPlayOutScoreboardTeam packet;
            if(scoreboardTeam == null) {
                // create team
                list.add(this.createTeam(team, friendlyFire));
                scoreboardTeam = this.getTeam(team.getName());
            }

            this.addPlayerToTeam(member.getOfflinePlayer().getName(), scoreboardTeam.getName());

            // status 3 means that all players containing the list are added ti this team
            list.add(new PacketPlayOutScoreboardTeam(scoreboardTeam, Collections.singletonList(member.getOfflinePlayer().getName()), 3));
        }

        return list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Updates a scoreboard team
     *
     * @param team the team you want to update
     * @return the packet that must be sent to every client
     */
    @Nullable
    public PacketPlayOutScoreboardTeam updateTeam(@NotNull Team team) {
        if(this.getTeam(team.getName()) != null) {
            ScoreboardTeam scoreboardTeam = this.getTeam(team.getName());

            scoreboardTeam.setPrefix(Optional.ofNullable(team.getPrefix()).orElse(""));
            scoreboardTeam.setDisplayName(Optional.ofNullable(team.getDisplayName()).orElse(""));
            scoreboardTeam.getPlayerNameSet().addAll(team.getPlayerNameSet());

            // status 0 means create this new team
            return new PacketPlayOutScoreboardTeam(scoreboardTeam, 2);
        }

        return null;
    }

    /**
     * Creates a scoreboard team of a team
     *
     * @param team the name of the team
     * @param friendlyFire if friendly fire should be enabled
     * @return the packet that should be sent to every client
     */
    @Nullable
    public PacketPlayOutScoreboardTeam createTeam(@NotNull Team team, boolean friendlyFire) {
        if(this.getTeam(team.getName()) == null) {
            ScoreboardTeam scoreboardTeam = this.createTeam(team.getName());

            scoreboardTeam.setPrefix(Optional.ofNullable(team.getPrefix()).orElse(""));
            scoreboardTeam.setDisplayName(Optional.ofNullable(team.getDisplayName()).orElse(""));
            scoreboardTeam.getPlayerNameSet().addAll(team.getPlayerNameSet());
            scoreboardTeam.setAllowFriendlyFire(friendlyFire);

            // status 0 means create this new team
            return new PacketPlayOutScoreboardTeam(scoreboardTeam, 0);
        }

        return null;
    }

    /**
     * Returns the display name of a player or console
     * @param sender the player or console
     * @return the correct displayname, cannot be null
     */
    @NotNull
    public String getPrefix(CommandSender sender) {
        if(sender instanceof OfflinePlayer) {
            // get Prefix from Scoreboard
            return Optional.ofNullable(Tablist.getInstance().getPlayerTeam(sender.getName()))
                    .map(ScoreboardTeam::getPrefix)
                    .orElse(ChatColor.translateAlternateColorCodes('&', "&8[&7Kein Team&8] &7"));
        }

        return ChatColor.translateAlternateColorCodes('&', "&8[&4TERMINAL&8] &7");
    }
}
