package de.rytrox.varo.gamestate;

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

}
