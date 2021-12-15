package de.rytrox.gamestate;

import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameState {

    private final String identifier;
    private final List<Listener> registeredListener;

    public GameState(String identifier) {
        this.identifier = identifier;
        this.registeredListener = new ArrayList<>();
    }

    public GameState(String identifier, Listener... listeners) {
        this.identifier = identifier;
        this.registeredListener = Arrays.asList(listeners);
    }

    public void registerListener(Listener listener) {
        this.registeredListener.add(listener);
    }

    public boolean unregisterListener(Listener listener) {
        return this.registeredListener.remove(listener);
    }

    public List<Listener> getRegisteredListener() {
        return registeredListener;
    }

    public String getIdentifier() {
        return identifier;
    }
}
