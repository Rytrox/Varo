package de.rytrox.gamestate.timeout;

import de.rytrox.varo.teams.TeamManager;
import org.jetbrains.annotations.NotNull;

public class SetupPhase implements GameState {

    private TeamManager teamManager;

    public SetupPhase(@NotNull TeamManager manager) {
        this.teamManager = manager;
    }

    @Override
    public void onEnable() {
        // Plugin initialisierung für Setupphase hier
    }

    @Override
    public void onDisable() {
        // Plugin Deaktivierung für Setupphase hier
    }

    @Override
    public @NotNull GameState nextState() {
        return new IngamePhase(teamManager);
    }
}
