package de.rytrox.varo.game.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that will be triggered when the game starts
 */
public class GameDayStartEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }
}
