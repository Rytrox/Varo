package de.rytrox.varo;

import de.rytrox.varo.message.chatlog.ChatLogCommand;
import de.rytrox.varo.database.entity.*;
import de.rytrox.varo.game.countdown.CountdownCommand;
import de.rytrox.varo.database.repository.ChatLogRepository;
import de.rytrox.varo.game.GameService;
import de.rytrox.varo.gamestate.GameStateHandler;
import de.rytrox.varo.game.moderation.ModeratorManager;
import de.rytrox.varo.game.resurrection.PlayerResurrectionListener;
import de.rytrox.varo.teams.ReplyCommand;
import de.rytrox.varo.teams.scoreboard.ScoreBoardManager;
import de.rytrox.varo.game.GameTimeService;
import de.rytrox.varo.teams.MessageCommand;
import de.rytrox.varo.teams.TeamManager;
import de.rytrox.varo.gamestate.GamestateCommand;
import de.rytrox.varo.message.MessageListener;
import de.rytrox.varo.game.resurrection.PlayerSkullDropService;
import de.rytrox.varo.message.MessageService;
import de.rytrox.varo.utils.ConfigCreator;
import de.rytrox.varo.utils.JsonConfig;
import de.rytrox.varo.world.worldborder.WorldBorderHandler;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.datasource.DataSourceConfig;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.h2.Driver;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public final class Varo extends JavaPlugin {

    private Database database;
    private JsonConfig stateStorage;

    private ChatLogRepository chatLogRepository;
    private GameStateHandler gameStateHandler;
    private MessageService messageService;
    private WorldBorderHandler worldBorderHandler;
    private TeamManager teamManager;
    private ScoreBoardManager scoreBoardManager;
    private ModeratorManager moderatorManager;

    @Override
    public void onEnable() {

        // Plugin startup logic
        installDDL();
        saveDefaultConfig();
        saveDefaultJsonConfig();

        this.gameStateHandler = new GameStateHandler(this);
        this.worldBorderHandler = new WorldBorderHandler(this);
        this.teamManager = new TeamManager(this);
        this.scoreBoardManager = new ScoreBoardManager(this);
        this.messageService = new MessageService(this, gameStateHandler);
        this.messageService.writeMessage("Der Server wurde gestartet!", MessageService.DiscordColor.CYAN);

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new MessageListener(this), this);
        pluginManager.registerEvents(new PlayerSkullDropService(), this);
        pluginManager.registerEvents(new PlayerResurrectionListener(messageService), this);
        pluginManager.registerEvents(new GameService(this), this);

        this.moderatorManager = new ModeratorManager(this);

        this.getCommand("gamestate").setExecutor(new GamestateCommand(gameStateHandler));
        this.getCommand("message").setExecutor(new MessageCommand(this));
        this.getCommand("reply").setExecutor(new ReplyCommand());
        this.getCommand("countdown").setExecutor(new CountdownCommand(this));

        this.chatLogRepository = new ChatLogRepository(getDB());
        this.getCommand("chatlog").setExecutor(new ChatLogCommand(this));
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
                SpawnPoint.class,
                ChatLogPrimaryKey.class,
                ChatLog.class,
                PlayerTimeStatistic.class
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

    public ChatLogRepository getChatLogRepository() {
        return chatLogRepository;
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

    public void saveDefaultJsonConfig() {
        try {
            File file = new ConfigCreator(this).copyDefaultFile(Paths.get("states.json"), Paths.get("states.json"));

            this.stateStorage = new JsonConfig(file);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Cannot create states.json File", e);
        }
    }

    @NotNull
    public JsonConfig getStateStorage() {
        return stateStorage;
    }

    public void saveStateStorage() {
        try {
            this.stateStorage.save(new File(getDataFolder(), "states.json"));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Cannot save states.json", e);
        }
    }

    @NotNull
    public GameStateHandler getGameStateHandler() {
        return gameStateHandler;
    }

    @NotNull
    public MessageService getMessageService() {
        return messageService;
    }
}
