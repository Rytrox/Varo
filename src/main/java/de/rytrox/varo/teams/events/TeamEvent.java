package de.rytrox.varo.teams.events;

import de.rytrox.varo.database.entity.Team;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract events that manages TeamEvents
 *
 * @author Timeout
 */
public abstract class TeamEvent extends Event {

    protected Team team;

    public TeamEvent(@NotNull Team team) {
        this.team = team;
    }

    /**
     * Returns the Team
     *
     * @return the Team. Cannot be null
     */
    @NotNull
    public Team getTeam() {
        return team;
    }

    /**
     * Sets the new Team
     *
     * @param team the new team. Cannot be null
     */
    public void setTeam(@NotNull Team team) {
        this.team = team;
    }
}
