package de.rytrox.gamestate.mixed;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class GameState implements State {

    private final String identifier;
    private final List<Listener> registeredListener;

    private State nextState;

    public GameState(String identifier, State nextState) {
        this.identifier = identifier;
        this.registeredListener = new ArrayList<>();
        this.nextState = nextState;
    }

    public GameState(String identifier, Listener... listeners) {
        this.identifier = identifier;
        this.registeredListener = Arrays.asList(listeners);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        GameState gameState = (GameState) o;

        return new EqualsBuilder().append(identifier, gameState.identifier).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(identifier).toHashCode();
    }

    public void registerListener(Listener listener) {
        this.registeredListener.add(listener);
    }

    public boolean unregisterListener(Listener listener) {
        HandlerList.unregisterAll(listener);

        return this.registeredListener.remove(listener);
    }

    public List<Listener> getRegisteredListener() {
        return registeredListener;
    }

    @Override
    public @Nullable State next() {
        return nextState;
    }

    public void next(State state) {

    }

    public @NotNull String getIdentifier() {
        return identifier;
    }
}
