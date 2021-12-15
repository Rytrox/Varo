package de.rytrox.gamestate.mixed.example;

import de.rytrox.gamestate.mixed.State;
import de.rytrox.varo.Varo;
import de.rytrox.varo.scoreboard.ScoreBoardManager;
import de.rytrox.varo.teams.GameTimeService;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FifthPhase extends State {

    private final Varo main;

    private final GameTimeService gameTimeService;
    private final ScoreBoardManager scoreBoardManager;

    public FifthPhase(@NotNull Varo main,
                      @NotNull GameTimeService gameTimeService,
                      @NotNull ScoreBoardManager scoreBoardManager) {
        super("forth");

        this.main = main;
        this.gameTimeService = gameTimeService;
        this.scoreBoardManager = scoreBoardManager;
    }


    /**
     * Diese Methode möchte ich für die Vererbung offen lassen!
     */
    @Override
    public void onEnable() {
        // Da die beiden Listener schon registriert sind, brauch ich hier nichts tun. Yay!!
    }

    /**
     * Diese Methode möchte ich für die Vererbung offen lassen!
     */
    @Override
    public void onDisable() {
        HandlerList.unregisterAll(gameTimeService);
        HandlerList.unregisterAll(scoreBoardManager);
    }

    @Override
    public @Nullable State next() {
        // Hiermit aktualisiere ich wieder zu den vorher definierten States
        return main.getGameStateHandler().getGameState("first");
    }
}
