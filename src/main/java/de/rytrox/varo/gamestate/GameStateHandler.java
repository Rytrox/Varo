package de.rytrox.varo.gamestate;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class GameStateHandler {

    private static final GameStateHandler instance = new GameStateHandler();
    private static JavaPlugin main;

    private List<GameState> gameStates = new ArrayList<>();

    private int currentGameState = -1;

    private GameStateHandler() {}

    /**
     * Returns an instance of the gamestatehandler
     * @return an instance of the GamestateHandler
     */
    public static GameStateHandler getInstance(JavaPlugin main) {
        if(GameStateHandler.main == null) {
            GameStateHandler.main = main;
            if(main.getCommand("gamestate").getExecutor() == null) {
                main.getCommand("gamestate").setExecutor(new GamestateCommand(main));
            }
        }
        return instance;
    }

    /**
     * Searches for a gamestate with the specified name
     */
    private Optional<GameState> findGameState(String gameState) {
        return this.gameStates.stream().filter(gs -> gs.getName().equalsIgnoreCase(gameState)).findAny();
    }

    /**
     * Registers a new gamestate
     * @param gameState
     */
    public void registerGameState(String gameState) {
        if(findGameState(gameState).isPresent()) {
            throw new IllegalArgumentException("The GameState " + gameState + " has already been registered");
        }
        gameStates.add(new GameState(gameState.toLowerCase()));
    }

    /**
     * Registers a listener on a specified gamestate
     */
    public void registerListener(String gameState, Listener listener) {
        Optional<GameState> gameStateOptional = findGameState(gameState);
        if(!gameStateOptional.isPresent()) {
            throw new IllegalArgumentException("The GameState " + gameState + " is not registered. Therefore no listener can be registered");
        }
        gameStateOptional.get().registerListener(listener);
    }

    /**
     * Returns a list of all available game states
     * @return all available game states
     */
    public List<String> getGameStates() {
        return this.gameStates.stream().map(GameState::getName).collect(Collectors.toList());
    }

    /**
     * Returns the current game state
     * @return the current game state
     */
    public String getCurrentGameState() {
        return this.gameStates.get(currentGameState).getName();
    }

    /**
     * Sets the current game state
     * @param gameState the new game state
     */
    public void setCurrentGameState(String gameState) {
        Optional<GameState> gameStateOptional = findGameState(gameState);
        if(!gameStateOptional.isPresent()) {
            throw new IllegalArgumentException("Cannot set gamestate to " + gameState +" because it has not been registered");
        }

        setCurrentGameState(gameStates.indexOf(gameStateOptional.get()));
    }

    /**
     * Sets the current game state and registers events
     * @param gameState the index of the new gamestate
     */
    private void setCurrentGameState(int gameState) {
        if(gameState < 0 || gameState >= gameStates.size()) {
            throw new IllegalArgumentException("Cannot set gamestate to index " + gameState + ". Index is out of bounds");
        }

        // unregister old listener
        if(this.currentGameState != -1) {
            GameState oldGameState = this.gameStates.get(this.currentGameState);
            oldGameState.getRegisteredListener().forEach(HandlerList::unregisterAll);
        }

        this.currentGameState = gameState;

        GameState newGameState = this.gameStates.get(this.currentGameState);
        newGameState.getRegisteredListener().forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, main));
    }

    /**
     * switches to the next game state
     */
    public void nextGameState() {
        setCurrentGameState((this.currentGameState + 1) % this.gameStates.size());
    }


    private static class GameState {

        private final String name;
        private List<Listener> registeredListener;

        public GameState(String name) {
            this.name = name;
            this.registeredListener = new ArrayList<>();
        }

        public void registerListener(Listener listener) {
            this.registeredListener.add(listener);
        }

        public String getName() {
            return name;
        }

        public List<Listener> getRegisteredListener() {
            return registeredListener;
        }

    }
}
