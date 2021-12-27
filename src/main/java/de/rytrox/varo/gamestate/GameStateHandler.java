package de.rytrox.varo.gamestate;

import de.rytrox.varo.Varo;
import de.rytrox.varo.gamestate.events.GamestateChangeEvent;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class GameStateHandler {

    private final Varo main;

    private GameState currentGameState;

    public GameStateHandler(@NotNull Varo main) {
        this.main = main;
        this.currentGameState = GameState.valueOf(main.getStateStorage().getString("state", "SETUP"));
    }

    /**
     * Returns the current game state
     * @return the current game state
     */
    public GameState getCurrentGameState() {
        return currentGameState;
    }

    /**
     * Sets the current game state
     * @param gameState the new game state
     */
    public void setCurrentGameState(GameState gameState) {
        GamestateChangeEvent changeEvent = new GamestateChangeEvent(this.currentGameState, gameState);
        Bukkit.getPluginManager().callEvent(changeEvent);

        this.currentGameState = changeEvent.getNext();
        this.main.getStateStorage().set("state", changeEvent.getNext().name());
        this.main.saveStateStorage();
    }

    /**
     * switches to the next game state
     */
    public void nextGameState() {
        setCurrentGameState(GameState.values()[(currentGameState.ordinal() + 1) % GameState.values().length]);
    }

    public enum GameState {

        SETUP,
        PRE_GAME,
        MAIN,
        FINAL,
        POST

    }

}
