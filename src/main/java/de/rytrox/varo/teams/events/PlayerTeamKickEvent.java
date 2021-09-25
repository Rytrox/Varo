package de.rytrox.varo.teams.events;

import de.rytrox.varo.database.entity.Team;
import de.rytrox.varo.database.entity.TeamMember;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that will be emitted when a player was kicked out of a team
 *
 * @author Timeout
 */
public class PlayerTeamKickEvent extends TeamEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final TeamMember teamMember;
    private boolean cancel;

    public PlayerTeamKickEvent(@NotNull Team team, @NotNull TeamMember member) {
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
     * Returns a team member of the player who was kicked out of the team
     *
     * @return the team member of the player
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
