package de.rytrox.gamestate.mixed;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation Notes:
 *
 * - Ich habe mich hier explizit gegen ein Singleton-Pattern entschieden. Diese Library soll an ein Plugin gebunden werden.
 *   Ein Singleton-Pattern würde hier nur hindern und sich gegenseitig beeinflussen!
 */
public class GameStateHandler {

    private final JavaPlugin main;
    private final LinkedList<GameState> gameStates = new LinkedList<>();

    private State currentState;


    public GameStateHandler(@NotNull JavaPlugin main) {
        this.main = main;
    }

    /**
     * Searches for a gamestate with the specified identifier
     */
    @NotNull
    private Optional<GameState> findGameState(String other) {
        return this.gameStates.stream()
                .filter(gs -> gs.getIdentifier().equalsIgnoreCase(other))
                .findAny();
    }

    @NotNull
    public GameState getGameState(String identifier) {
        return findGameState(identifier)
                .orElseThrow(() -> new IllegalArgumentException("Cannot set gamestate to " + identifier + " because it has not been registered"));
    }

    /**
     * Registers new gamestates
     * @param states the gamestates you want to register
     */
    public void registerGameStates(@NotNull GameState... states) {
        for(GameState state : states) {
            registerGameState(state, this.gameStates.peekLast());
        }

        this.currentState = this.gameStates.peekFirst();
    }

    /**
     * Registers a new gamestate
     * @param gameState the gamestate you want to register
     */
    public void registerGameState(GameState gameState, GameState before) {
        if(findGameState(gameState.getIdentifier()).isPresent()) {
            throw new IllegalArgumentException("The GameState " + gameState + " has already been registered");
        }

        before.next(gameState);
        gameStates.add(gameState);
    }

        /**
         * Returns a list of all available game states
         * @return all available game states
         */
        public List<State> getGameStates() {
            return new ArrayList<>(this.gameStates);
        }

        /**
         * Returns the current game state
         * @return the current game state
         */
        public String getCurrentGameState() {
            return this.currentState.getIdentifier();
        }

        /**
         * Sets the current game state
         * @param gameState the new game state
         */
        public void setCurrentGameStateByIdentifier(String gameState) {
            GameState foundState = getGameState(gameState);

            this.disableCurrentPhase();
            this.currentState = foundState;
            this.enableCurrentPhase();
        }

        public void setCurrentGameState(@NotNull AbstractState gameState) {
            // Disable all old listener
            this.disableCurrentPhase();

            this.currentState = gameState;
            gameState.onEnable();
        }

        private void disableCurrentPhase() {
            if(this.currentState instanceof GameState) {
                ((GameState) currentState).getRegisteredListener()
                        .forEach(HandlerList::unregisterAll);
            } else {
                ((AbstractState) this.currentState).onDisable();
            }
        }

        private void enableCurrentPhase() {
            if(this.currentState instanceof GameState) {
                ((GameState) currentState).getRegisteredListener()
                        .forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, main));
            } else {
                ((AbstractState) this.currentState).onEnable();
            }
        }

        /**
         * switches to the next game state
         */
        public void nextGameState() {
            disableCurrentPhase();
            this.currentState = this.currentState.next();
        }
}
