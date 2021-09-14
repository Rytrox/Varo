package de.rytrox.varo.database.entity;

import de.rytrox.varo.database.enums.PlayerStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.UUID;

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
}
