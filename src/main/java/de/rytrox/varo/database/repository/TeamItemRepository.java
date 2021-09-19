package de.rytrox.varo.database.repository;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.TeamItem;
import de.rytrox.varo.database.entity.TeamMember;

import io.ebean.Database;

import io.ebean.Finder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TeamItemRepository extends Finder<Integer, TeamItem> {

    private final Database database;
    private final TeamMemberRepository teamRepository;

    public TeamItemRepository(@NotNull Varo main, @NotNull TeamMemberRepository teamRepository) {
        super(TeamItem.class);
        this.database = main.getDB();
        this.teamRepository = teamRepository;
    }

    /**
     * Selects the Team-Inventory of a Team
     * Returns an empty list when the player is in no team.
     *
     * NOTE: Please use this method outside the Main-Thread
     *
     * @param playerID the uuid of the player
     * @return the
     */
    @Nullable
    public List<TeamItem> getTeamItems(@NotNull UUID playerID) {
        // search Team
        return Optional.ofNullable(teamRepository.getPlayerByUUID(playerID))
                .map(TeamMember::getTeam)
                .map((team) ->
                    database.find(TeamItem.class)
                            .where()
                            .eq("team", team)
                            .findList()
                )
                .orElse(null);
    }
}
