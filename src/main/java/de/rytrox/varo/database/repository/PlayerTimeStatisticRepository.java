package de.rytrox.varo.database.repository;

import de.rytrox.varo.database.entity.PlayerTimeStatistic;
import de.rytrox.varo.database.entity.TeamMember;
import io.ebean.Database;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PlayerTimeStatisticRepository {

    private final Database database;

    public PlayerTimeStatisticRepository(@NotNull Database database) {
        this.database = database;
    }

    /**
     * Increases the PlayerTime of all players by one. <br>
     * If the Player has reached the max limit it won't increase anything.
     *
     * @param maxDays
     */
    public void increaseDays(int maxDays) {
        this.database.find(PlayerTimeStatistic.class)
                .asUpdate()
                .setRaw("available_days = MIN(available_days + 1, " + maxDays + ")")
                .update();
    }
}
