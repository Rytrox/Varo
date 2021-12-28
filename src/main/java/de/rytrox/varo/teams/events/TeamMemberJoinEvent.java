package de.rytrox.varo.teams.events;

import de.rytrox.varo.database.entity.TeamMember;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event that will happen right after {@link org.bukkit.event.player.PlayerJoinEvent}
 * when a TeamMember joined the game
 *
 * @author Timeout
 */
public class TeamMemberJoinEvent extends PlayerEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final TeamMember member;


    public TeamMemberJoinEvent(@NotNull Player player, @NotNull TeamMember member) {
        super(player);

        this.member = member;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public TeamMember getMember() {
        return member;
    }
}
