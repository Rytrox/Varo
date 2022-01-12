package de.rytrox.varo.teams.events;

import de.rytrox.varo.database.entity.TeamMember;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Event that is triggered when a Team member dies
 *
 * @author Timeout
 */
public class TeamMemberDeathEvent extends TeamMemberEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final TeamMember killer;

    public TeamMemberDeathEvent(@NotNull TeamMember entity, @Nullable TeamMember killer) {
        super(entity);

        this.killer = killer;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Nullable
    public TeamMember getKiller() {
        return killer;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }
}
