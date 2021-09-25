package de.rytrox.varo.teams.events;

import de.rytrox.varo.database.entity.Team;
import de.rytrox.varo.database.entity.TeamMember;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that will be triggered when a Player joins a team
 *
 * @author Timeout
 */
public class PlayerTeamJoinEvent extends TeamEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final TeamMember teamMember;
    private boolean cancel;

    public PlayerTeamJoinEvent(@NotNull Team team, @NotNull TeamMember member) {
        super(team);

        this.teamMember = member;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    /**
     * Returns the TeamMember of the player that joins the team
     *
     * @return the team of the player
     */
    @NotNull
    public TeamMember getPlayer() {
        return teamMember;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancel = b;
    }
}
