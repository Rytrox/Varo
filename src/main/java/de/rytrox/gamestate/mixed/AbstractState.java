package de.rytrox.gamestate.mixed;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractState implements State {

    protected final String identifier;

    public AbstractState(@NotNull String identifier) {
        this.identifier = identifier;
    }

    /**
     * Diese Methode möchte ich für die Vererbung offen lassen!
     */
    public abstract void onEnable();

    /**
     * Diese Methode möchte ich für die Vererbung offen lassen!
     */
    public abstract void onDisable();

    @NotNull
    public String getIdentifier() {
        return identifier;
    }
}
