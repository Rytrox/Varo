package de.rytrox.varo.game.resurrection;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerResurrectionEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player resurrected;
    private final Player resurrector;

    public PlayerResurrectionEvent(Player resurrected, Player resurrector) {
        this.resurrected = resurrected;
        this.resurrector = resurrector;
    }

    public Player getResurrected() {
        return resurrected;
    }

    public Player getResurrector() {
        return resurrector;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
