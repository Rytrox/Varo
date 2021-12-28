package de.rytrox.varo.database.repository;

import de.rytrox.varo.database.entity.PlayerTimeStatistic;
import de.rytrox.varo.database.entity.TeamMember;
import io.ebean.Database;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Collectors;

public class PlayerTimeStatisticRepository {

    private final Database database;

    public PlayerTimeStatisticRepository(@NotNull Database database) {
        this.database = database;
    }

    /**
     * Increases the PlayerTime of all players by one. <br>
     * If the Player has reached the max limit it won't increase anything.
     *
     * @param maxDays the maximum amount of days a player can have
     */
    public void increaseDays(int maxDays) {
        // Ebean does not support this functionality...
        this.database.updateAll(
                this.database.find(PlayerTimeStatistic.class)
                        .findList()
                        .stream()
                        .peek((statistic) -> statistic.increaseDays(Math.min(maxDays, statistic.getAvailableDays() + 1)))
                        .collect(Collectors.toList())
        );
    }
}
