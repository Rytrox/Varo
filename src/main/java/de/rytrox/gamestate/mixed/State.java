package de.rytrox.gamestate.mixed;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for GameStates to manage
 */
public interface State {

    @Nullable
    State next();

    @NotNull
    String getIdentifier();
}
