package de.rytrox.varo.database.repository;

import de.rytrox.varo.database.entity.TeamMember;

import io.ebean.Database;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
}
