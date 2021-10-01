package de.rytrox.varo.database.repository;

import de.rytrox.varo.database.entity.Team;

import io.ebean.Database;

import io.ebean.Finder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Repository-Class for Teams
 *
 * @author Timeout
 */
public class TeamRepository {

    private final Database database;

    public TeamRepository(@NotNull Database database) {
        this.database = database;
    }

    /**
     * Searches a Team based on its name
     *
     * @param teamname the name of the Team
     * @return the found team. null if the Team cannot be found
     */
    @Nullable
    public Team findByName(@NotNull String teamname) {
        return this.database.find(Team.class)
                .where()
                .eq("name", teamname)
                .findOne();
    }

    /**
     * Returns a list containing all valid teamnames
     *
     * @return the list containing all team names
     */
    @NotNull
    public List<String> getAllTeamNames() {
        return this.database.find(Team.class)
                .select("name")
                .findIds();
    }
}
