package de.rytrox.varo.database.repository;

import com.avaje.ebean.EbeanServer;
import de.rytrox.varo.database.entity.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Repository-Class for Teams
 *
 * @author Timeout
 */
public class TeamRepository {

    private final EbeanServer database;

    public TeamRepository(@NotNull EbeanServer database) {
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
                .select("name")
                .where()
                .eq("name", teamname)
                .findUnique();
    }
}
