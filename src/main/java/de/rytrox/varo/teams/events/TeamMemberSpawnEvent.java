package de.rytrox.varo.teams.events;

import de.rytrox.varo.database.entity.TeamMember;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Event that will be triggered when a TeamMember joins the game
 *
 * @author Timeout
 */
public class TeamMemberSpawnEvent extends TeamMemberEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean cancel;
    private String cancelMessage;

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

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancel = b;
    }

    @Nullable
    public String getCancelMessage() {
        return cancelMessage;
    }

    public void setCancelMessage(@Nullable String cancelMessage) {
        this.cancelMessage = cancelMessage;
    }
}
