package de.rytrox.varo;

import de.rytrox.gamestate.sether.GameState;
import de.rytrox.gamestate.sether.GameStateHandler;
import de.rytrox.varo.database.entity.SpawnPoint;
import de.rytrox.varo.countdown.CountdownCommand;
import de.rytrox.varo.database.entity.Team;
import de.rytrox.varo.database.entity.TeamItem;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.database.repository.TeamMemberRepository;
import de.rytrox.varo.moderation.ModeratorManager;
import de.rytrox.varo.moderation.ModeratorTeleporter;
import de.rytrox.varo.resurrection.PlayerResurrectionListener;
import de.rytrox.varo.scoreboard.ScoreBoardManager;
import de.rytrox.varo.teams.GameTimeService;
import de.rytrox.varo.teams.MessageCommand;
import de.rytrox.varo.teams.TeamManager;
import de.rytrox.varo.discord.DiscordListener;
import de.rytrox.varo.resurrection.PlayerSkullDropService;
import de.rytrox.varo.discord.MessageService;

import de.rytrox.varo.teams.TeamMemberGhostService;
import de.rytrox.varo.teams.inventory.TeamInventoryManager;
import de.rytrox.varo.teams.spawnpoint.SpawnPointService;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.datasource.DataSourceConfig;

import de.rytrox.varo.worldborder.WorldBorderHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.h2.Driver;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public final class Varo extends JavaPlugin {

    private final de.rytrox.gamestate.mixed.GameStateHandler gameStateHandler = new de.rytrox.gamestate.mixed.GameStateHandler(this);

    private Database database;

    private WorldBorderHandler worldBorderHandler;
    private TeamManager teamManager;
    private ScoreBoardManager scoreBoardManager;
    private ModeratorManager moderatorManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        installDDL();
        saveDefaultConfig();

        registerServices();

        MessageService.getInstance(this).writeMessage("Der Server wurde gestartet!", MessageService.DiscordColor.CYAN);

        this.getCommand("message").setExecutor(new MessageCommand());
        this.getCommand("countdown").setExecutor(new CountdownCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        DatabaseFactory.shutdown();
    }

    @Contract(pure = true)
    @Override
    public @NotNull List<Class<?>> getDatabaseClasses() {
        return Arrays.asList(
                TeamMember.class,
                TeamItem.class,
                Team.class,
                SpawnPoint.class
        );
    }

    @Override
    protected void installDDL() {
        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            DriverManager.registerDriver(new Driver());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        DatabaseConfig config = getDatabaseConfig();
        this.database = DatabaseFactory.create(config);

        Thread.currentThread().setContextClassLoader(originalContextClassLoader);
    }

    @NotNull
    public Database getDB() {
        return database;
    }

    @NotNull
    public TeamManager getTeamManager() {
        return teamManager;
    }

    @NotNull
    public ScoreBoardManager getScoreBoardManager() {
        return scoreBoardManager;
    }

    @NotNull
    public WorldBorderHandler getWorldBorderHandler() {
        return worldBorderHandler;
    }

    @NotNull
    public ModeratorManager getModeratorManager() {
        return moderatorManager;
    }

    @NotNull
    public de.rytrox.gamestate.mixed.GameStateHandler getGameStateHandler() {
        return gameStateHandler;
    }

    @NotNull
    private DatabaseConfig getDatabaseConfig() {
        DatabaseConfig config = new DatabaseConfig();
        config.setDdlRun(!(new File(getDataFolder(), "Varo.h2.db").exists()));
        config.setDdlGenerate(!(new File(getDataFolder(), "Varo.h2.db").exists()));
        config.setDdlCreateOnly(true);
        config.setRegister(true);
        config.setDefaultServer(true);
        config.setClasses(getDatabaseClasses());
        config.setAutoPersistUpdates(true);
        config.setDatabasePlatform(new H2Platform());

        DataSourceConfig sourceConfig = new DataSourceConfig();
        sourceConfig.setDriver("org.h2.Driver");
        sourceConfig.setUsername("sa");
        sourceConfig.setPassword("sa");
        sourceConfig.setUrl(String.format("jdbc:h2:%s;MV_STORE=false", new File(getDataFolder(), "Varo").getAbsolutePath()));
        config.setDataSourceConfig(sourceConfig);

        return config;
    }

    /**
     * Creates instances of all services and groups them in GameStates
     */
    private void registerServices() {

        DiscordListener discordListener = new DiscordListener(this);
        this.moderatorManager = new ModeratorManager(this);
        ModeratorTeleporter moderatorTeleporter = new ModeratorTeleporter(this, moderatorManager);
        PlayerResurrectionListener playerResurrectionListener = new PlayerResurrectionListener();
        PlayerSkullDropService playerSkullDropService = new PlayerSkullDropService();
        this.scoreBoardManager = new ScoreBoardManager(this);
        GameTimeService gameTimeService = new GameTimeService(this);
        this.teamManager = new TeamManager(this);
        TeamMemberGhostService teamMemberGhostService = new TeamMemberGhostService(this);

        TeamMemberRepository teamMemberRepository = new TeamMemberRepository(getDB());

        TeamInventoryManager teamInventoryManager = new TeamInventoryManager(this, teamMemberRepository);
        SpawnPointService spawnPointService = new SpawnPointService(this, teamMemberRepository);
        this.worldBorderHandler = new WorldBorderHandler(this);


        GameState gameStateSetup = new GameState("Setup", teamManager, spawnPointService);
        GameState gameStatePreGame = new GameState("Pre-Game", discordListener, moderatorManager, moderatorTeleporter, scoreBoardManager, spawnPointService);
        GameState gameStateMain = new GameState("Main",
                discordListener, moderatorManager, moderatorTeleporter,
                playerResurrectionListener, playerSkullDropService, scoreBoardManager,
                gameTimeService, teamMemberGhostService, teamInventoryManager);
        GameState gameStateFinal = new GameState("Final",
                discordListener, moderatorManager, moderatorTeleporter,
                playerResurrectionListener, playerSkullDropService, scoreBoardManager,
                gameTimeService, teamMemberGhostService, teamInventoryManager);
        GameState gameStatePost = new GameState("Post",
                moderatorManager, moderatorTeleporter, scoreBoardManager);

        GameStateHandler.getInstance(this).registerGameStates(
                gameStateSetup,
                gameStatePreGame,
                gameStateMain,
                gameStateFinal,
                gameStatePost);
    }
}
