package de.rytrox.varo.database.repository;

import de.rytrox.varo.database.entity.Team;

import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.database.enums.PlayerStatus;
import io.ebean.Database;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

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
     * @return the found team
     */
    @NotNull
    public Optional<Team> findByName(@NotNull String teamname) {
        return this.database.find(Team.class)
                .where()
                .eq("name", teamname)
                .findOneOrEmpty();
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

    /**
     * Returns all Teams with alive members
     * @return all teams with alive members
     */
    public List<Team> getAllTeamsWithAliveMembers() {
        return this.database.find(Team.class)
                .where()
                .idIn(
                        this.database.find(TeamMember.class)
                                .setDistinct(true)
                                .select("team")
                                .where()
                                .eq("status", PlayerStatus.ALIVE)
                                .findSingleAttributeList()
                ).findList();
    }
}
