package de.rytrox.varo.database.repository;

import com.avaje.ebean.EbeanServer;
import de.rytrox.varo.database.entity.TeamMember;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Repository Class for {@link TeamMember}
 *
 * @author Timeout
 */
public class TeamMemberRepository {

    private final EbeanServer database;

    public TeamMemberRepository(@NotNull EbeanServer database) {
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
        return this.database.find(TeamMember.class)
                .select("uuid")
                .where()
                .eq("uuid", uuid.toString())
                .findUnique();
    }
}
