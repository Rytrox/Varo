package de.rytrox.varo.teams.events;

import de.rytrox.varo.database.entity.TeamMember;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that will be triggered when a TeamMember joins the game
 *
 * @author Timeout
 */
public class TeamMemberSpawnEvent extends TeamMemberEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public TeamMemberSpawnEvent(@NotNull Player player, @NotNull TeamMember member) {
        super(player, member);
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }
}
