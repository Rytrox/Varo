package de.rytrox.gamestate.mixed.example;

import de.rytrox.gamestate.mixed.State;
import de.rytrox.varo.Varo;
import de.rytrox.varo.discord.DiscordListener;
import de.rytrox.varo.scoreboard.ScoreBoardManager;
import de.rytrox.varo.teams.GameTimeService;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForthPhase extends State {

    private final Varo main;

    private final DiscordListener discordListener;
    private final GameTimeService gameTimeService;
    private final ScoreBoardManager scoreBoardManager;

    public ForthPhase(@NotNull Varo main,
                      @NotNull DiscordListener discordListener,
                      @NotNull GameTimeService gameTimeService,
                      @NotNull ScoreBoardManager scoreBoardManager) {
        super("forth");

        this.main = main;
        this.discordListener = discordListener;
        this.gameTimeService = gameTimeService;
        this.scoreBoardManager = scoreBoardManager;
    }

    /**
     * Diese Methode möchte ich für die Vererbung offen lassen!
     */
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(discordListener, main);
        Bukkit.getPluginManager().registerEvents(gameTimeService, main);
        Bukkit.getPluginManager().registerEvents(scoreBoardManager, main);

    }

    /**
     * Diese Methode möchte ich für die Vererbung offen lassen!
     */
    @Override
    public void onDisable() {
        HandlerList.unregisterAll(discordListener);
    }

    @Override
    public @Nullable State next() {
        return null;
    }
}
