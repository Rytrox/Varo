package de.rytrox.varo.teams.events;

import de.rytrox.varo.database.entity.Team;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that will be emitted when a team is going to be modified
 *
 * @author Timeout
 */
public class TeamModifyEvent extends TeamEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean cancel;

    public TeamModifyEvent(@NotNull Team team) {
        super(team);
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancel = b;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }
}
