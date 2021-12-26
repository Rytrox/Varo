package de.rytrox.varo.gamestate.events;

import de.rytrox.varo.gamestate.GameStateHandler;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GamestateChangeEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final GameStateHandler.GameState previous;
    private final GameStateHandler.GameState next;

    public GamestateChangeEvent(@NotNull GameStateHandler.GameState previous, @NotNull GameStateHandler.GameState next) {
        this.previous = previous;
        this.next = next;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    @NotNull
    public GameStateHandler.GameState getPrevious() {
        return previous;
    }

    @NotNull
    public GameStateHandler.GameState getNext() {
        return next;
    }
}
