package de.rytrox.varo.gamestate;

import de.rytrox.varo.Varo;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public enum GameState {

    SETUP("Setup"),
    PRE_GAME("Pre-Game"),
    START("Start"),
    MAIN("Main"),
    FINAL("Final"),
    POST("Post");

    private String name;

    GameState(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Registers all gamestates in the GameStateHandler
     */
    public static void register() {
        Arrays.stream(GameState.values()).forEach(gs ->
            GameStateHandler.getInstance(JavaPlugin.getPlugin(Varo.class))
                    .registerGameState(gs.getName()));
    }
}
