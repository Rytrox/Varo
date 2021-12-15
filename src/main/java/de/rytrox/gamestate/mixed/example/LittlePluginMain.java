package de.rytrox.gamestate.mixed.example;

import de.rytrox.gamestate.mixed.GameState;
import de.rytrox.varo.Varo;
import de.rytrox.varo.discord.DiscordListener;
import de.rytrox.varo.moderation.ModeratorManager;
import de.rytrox.varo.moderation.ModeratorTeleporter;
import de.rytrox.varo.resurrection.PlayerResurrectionListener;
import de.rytrox.varo.resurrection.PlayerSkullDropService;
import de.rytrox.varo.scoreboard.ScoreBoardManager;
import de.rytrox.varo.teams.GameTimeService;

import org.bukkit.plugin.java.JavaPlugin;

public class LittlePluginMain {

    private final Varo main = JavaPlugin.getPlugin(Varo.class);

    public void register() {
        DiscordListener discordListener = new DiscordListener(main);
        ModeratorManager moderatorManager = new ModeratorManager(main);
        ModeratorTeleporter moderatorTeleporter = new ModeratorTeleporter(main, moderatorManager);
        PlayerResurrectionListener playerResurrectionListener = new PlayerResurrectionListener();
        PlayerSkullDropService playerSkullDropService = new PlayerSkullDropService();
        ScoreBoardManager scoreBoardManager = new ScoreBoardManager(main);
        GameTimeService gameTimeService = new GameTimeService(main);

        // Zum einen ist das hier möglich
        GameState first = new GameState("Erster", discordListener, moderatorManager);
        GameState second = new GameState("Zweiter", moderatorTeleporter, playerResurrectionListener, playerSkullDropService);
        GameState third = new GameState("Dritter", scoreBoardManager, gameTimeService);

        // Hiernach ist der erste GameState an
        main.getGameStateHandler().registerGameStates(first, second, third);
        // Schalte ersten GameState aus und aktiviere zweiten
        main.getGameStateHandler().nextGameState();

        // Ich aktiviere jetzt die vererbte vierte Phase
        main.getGameStateHandler().setCurrentGameState(new ForthPhase(main, discordListener, gameTimeService, scoreBoardManager));
        // Gehe hier auf die fünfte Phase
        main.getGameStateHandler().nextGameState();
        // Und durch meine Custom Implementierung wieder auf die vordefinierte erste
        main.getGameStateHandler().nextGameState();
        
    }
}
