package de.rytrox.gamestate.mixed;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for GameStates to manage
 */
public abstract class State {

    protected final String identifier;

    public State(@NotNull String identifier) {
        this.identifier = identifier;
    }

    @Nullable
    public abstract State next();

    @NotNull
    public String getIdentifier() {
        return identifier;
    }

    public abstract void onEnable();

    public abstract void onDisable();
}
