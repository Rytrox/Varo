package de.rytrox.varo.database.entity;

import org.jetbrains.annotations.NotNull;

import javax.persistence.*;

@Entity
@Table(name = "PLAYER_TIME")
public class PlayerTimeStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "available_days", nullable = false)
    private Integer availableDays = 0;

    @Column(name = "blocked", nullable = false)
    private Boolean blocked = false;

    public PlayerTimeStatistic() {
        /* ONLY FOR JPA */
    }

    /**
     * Returns the amount of days the player can use
     *
     * @return the amount of available days of this player
     */
    @NotNull
    public Integer getAvailableDays() {
        return availableDays;
    }

    /**
     * Increases the amount of available days
     */
    public void increaseDays(@NotNull Integer maxDays) {
        this.availableDays = Math.min(maxDays, this.availableDays + 1);
    }
    /**
     * Decreases the amount of available days
     */
    public void decreaseDays() {
        this.availableDays--;
    }

    /**
     * Check if a player is currently blocked from playing. <br>
     * <br>
     * A player can be blocked when his days reached the minimal limit. <br>
     * After that he needs to wait until he reaches Day 0 in his score.
     *
     * @return the
     */
    public boolean isBlocked() {
        return this.blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
