package de.rytrox.varo.database.entity;

import de.rytrox.varo.database.enums.PlayerStatus;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.UUID;

/**
 * Entity for Players that are involved in the Game. <br>
 * This Entity also have the status included
 *
 * @author Timeout
 */
@Entity
@Table(name = "Players")
public class TeamMember {

    @Id
    @Column(name = "uuid", nullable = false, unique = true)
    private String uuid;

    @ManyToOne(cascade = CascadeType.ALL, targetEntity = Team.class)
    @JoinColumn(name = "team")
    private Team team;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status")
    private PlayerStatus status = PlayerStatus.NOT_REGISTERED;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TeamMember member = (TeamMember) o;

        return new EqualsBuilder().append(uuid, member.uuid).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(uuid).toHashCode();
    }

    /**
     * Internal Method for EBean to get the UUID of the player
     *
     * @return the UUID of the Player as a String
     */
    @ApiStatus.Internal
    public String getUuid() {
        return uuid;
    }

    /**
     * Internal Method for EBean to set the UUID of the player
     *
     * @param uuid the UUID of the new player
     */
    @ApiStatus.Internal
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Returns the UUID of the Player
     *
     * @return the unique id of the player
     */
    @NotNull
    public UUID getUniqueID() {
        return UUID.fromString(uuid);
    }

    /**
     * Sets the Player in this Object
     *
     * @param uuid the uuid of the player
     */
    public void setUniqueID(@NotNull UUID uuid) {
        this.uuid = uuid.toString();
    }

    /**
     * Returns the Team of this player
     *
     * @return the Team of the player
     */
    @Nullable
    public Team getTeam() {
        return team;
    }

    /**
     * Sets the team of the Player
     *
     * @param team the Team of the Player (Can be null)
     */
    public void setTeam(@Nullable Team team) {
        this.team = team;
    }

    /**
     * Returns the Status of the Player
     *
     * @return the Status of the player
     */
    @NotNull
    public PlayerStatus getStatus() {
        return status;
    }

    /**
     * Sets the Status of the player
     *
     * @param status the new status of the player
     */
    public void setStatus(@NotNull PlayerStatus status) {
        this.status = status;
    }

    /**
     * Returns the offline player of the team member
     *
     * @return the offline player
     */
    @NotNull
    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(getUniqueID());
    }

    /**
     * Returns the player of the team member. <br>
     * If the player is offline it will return null
     *
     * @return the player or null if the player is offline
     */
    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(getUniqueID());
    }
}
