package de.rytrox.gamestate.timeout;

import de.rytrox.varo.teams.TeamManager;
import org.jetbrains.annotations.NotNull;

public class IngamePhase implements GameState {

    public TeamManager manager;

    // Dependency-Injection ist möglich.
    public IngamePhase(TeamManager teamManager) {
        this.manager = teamManager;
    }

    @Override
    public void onEnable() {
        // Hier kann ich definieren, was in der GamePhase passieren soll!
    }

    @Override
    public void onDisable() {
        // Hier kann ich definieren, was am Ende der Ingamephase passieren soll!
    }

    @Override
    public @NotNull GameState nextState() {
        return new SetupPhase(manager); // Die darauf folgende GamePhase bestimme ich hier!
    }
}
