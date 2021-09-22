package de.rytrox.varo.teams.events;

import de.rytrox.varo.database.entity.TeamMember;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Calls when a TeamMember disconnects from the Server
 *
 * @author Timeout
 */
public class TeamMemberDisconnectEvent extends TeamMemberEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public TeamMemberDisconnectEvent(@NotNull Player who, @NotNull TeamMember teamMember) {
        super(who, teamMember);
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }
}
