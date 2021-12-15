package de.rytrox.gamestate;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
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
                main.getCommand("gamestate").setExecutor(new GameStateCommand(main));
            }
        }
        return instance;
    }

    /**
     * Searches for a gamestate with the specified identifier
     */
    private Optional<GameState> findGameState(String identifier) {
        return this.gameStates.stream().filter(gs -> gs.getIdentifier().equalsIgnoreCase(identifier)).findAny();
    }

    /**
     * Registers new gamestates
     * @param gameStates the gamestates you want to register
     */
    public void registerGameStates(GameState... gameStates) {
        for(GameState gameState : gameStates) {
            registerGameState(gameState);
        }
    }

    /**
     * Registers a new gamestate
     * @param gameState the gamestate you want to register
     */
    public void registerGameState(GameState gameState) {
        if(findGameState(gameState.getIdentifier()).isPresent()) {
            throw new IllegalArgumentException("The GameState " + gameState + " has already been registered");
        }
        gameStates.add(gameState);
    }

    /**
     * Returns a list of all available game states
     * @return all available game states
     */
    public List<String> getGameStateIdentifiers() {
        return this.gameStates.stream().map(GameState::getIdentifier).collect(Collectors.toList());
    }

    /**
     * Returns a list of all available game states
     * @return all available game states
     */
    public List<GameState> getGameStates() {
        return this.gameStates;
    }

    /**
     * Returns the current game state
     * @return the current game state
     */
    public String getCurrentGameState() {
        return this.gameStates.get(currentGameState).getIdentifier();
    }

    /**
     * Sets the current game state
     * @param gameState the new game state
     */
    public void setCurrentGameStateByIdentifier(String gameState) {
        Optional<GameState> gameStateOptional = findGameState(gameState);
        if(!gameStateOptional.isPresent()) {
            throw new IllegalArgumentException("Cannot set gamestate to " + gameState +" because it has not been registered");
        }

        this.setCurrentGameStateByIndex(gameStates.indexOf(gameStateOptional.get()));
    }

    /**
     * Sets the current game state and registers events
     * @param gameState the index of the new gamestate
     */
    private void setCurrentGameStateByIndex(int gameState) {
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
        setCurrentGameStateByIndex((this.currentGameState + 1) % this.gameStates.size());
    }
}
