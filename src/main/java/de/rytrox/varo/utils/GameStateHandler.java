package de.rytrox.varo.utils;

public class GameStateHandler {

    private GameState currentGameState;

    public GameStateHandler(GameState currentGameState) {
        this.currentGameState = currentGameState;
    }

    public GameStateHandler() {
        this.currentGameState = GameState.SETUP;
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
        this.currentGameState = gameState;
    }

    /**
     * switches to the next game state
     */
    public void nextGameState() {
        this.currentGameState = GameState.values()[(currentGameState.ordinal() + 1) % GameState.values().length];
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
