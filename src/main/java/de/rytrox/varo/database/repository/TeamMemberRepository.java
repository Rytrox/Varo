package de.rytrox.varo.database.repository;

import de.rytrox.varo.database.entity.TeamMember;

import de.rytrox.varo.database.enums.PlayerStatus;
import io.ebean.Database;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Class for {@link TeamMember}
 *
 * @author Timeout
 */
public class TeamMemberRepository {

    private final Database database;

    public TeamMemberRepository(@NotNull Database database) {
        this.database = database;
    }

    /**
     * Returns the Member
     *
     * @param player the player
     * @return the member and the player's stats. Can be null
     */
    @Nullable
    public TeamMember getPlayer(@NotNull OfflinePlayer player) {
        return getPlayerByUUID(player.getUniqueId());
    }

    /**
     * Searches for a player in Database. Returns an empty optional if the player could not be found
     *
     * @param player the player you want to search
     * @return the uuid of the player
     */
    @NotNull
    public Optional<TeamMember> findPlayer(@NotNull OfflinePlayer player) {
        return findPlayerByUUID(player.getUniqueId());
    }

    /**
     * Returns the Member by its UUID
     *
     * @param uuid the UUID of the Player
     * @return the member of the Team. Can be null
     */
    @Nullable
    public TeamMember getPlayerByUUID(@NotNull UUID uuid) {
        return findPlayerByUUID(uuid)
                .orElse(null);
    }

    /**
     * Searches the Member based of its UUID. <br>
     * Returns an empty Optional if the member cannot be found
     *
     * @param uuid the UUID of the Player
     * @return the team member object of the player
     */
    @NotNull
    public Optional<TeamMember> findPlayerByUUID(@NotNull UUID uuid) {
        return this.database.find(TeamMember.class)
                .where()
                .eq("uuid", uuid.toString())
                .findOneOrEmpty();
    }

    /**
     * returns the total amount of participants
     * @return total amount of participants
     */
    public int getTotalPlayerAmount() {
        return this.database.find(TeamMember.class)
                .findCount();
    }

    /**
     * returns the total amount of participants
     * @return total amount of participants
     */
    public int getAlivePlayerAmount() {
        return this.database.find(TeamMember.class)
                .where()
                .eq("status", PlayerStatus.ALIVE)
                .findCount();
    }

    /**
     * Searches all Members that are currently online. <br>
     *
     * @return all online members. Cannot be null
     */
    @NotNull
    public List<TeamMember> getOnlineMembers() {
        return this.database.find(TeamMember.class)
                .where()
                .idIn(Bukkit.getOnlinePlayers()
                        .stream()
                        .map((player) -> player.getUniqueId().toString())
                        .toArray())
                .findList();
    }
}
