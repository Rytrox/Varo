package de.rytrox.varo.gamestate;

import de.rytrox.varo.gamestate.events.GamestateChangeEvent;
import org.bukkit.Bukkit;

public class GameStateHandler {

    private static final GameStateHandler instance = new GameStateHandler();
    private GameState currentGameState;

    private GameStateHandler() {
        this.currentGameState = GameState.SETUP;
    }

    /**
     * Returns an instance of the gamestatehandler
     * @return an instance of the GamestateHandler
     */
    public static GameStateHandler getInstance() {
        return instance;
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
        START,
        MAIN,
        FINAL,
        POST

    }

}
