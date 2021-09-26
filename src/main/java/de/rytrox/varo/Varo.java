package de.rytrox.varo;

import de.rytrox.varo.database.entity.Team;
import de.rytrox.varo.database.entity.TeamItem;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.scoreboard.ScoreBoardManager;
import de.rytrox.varo.teams.MessageCommand;
import de.rytrox.varo.teams.TeamManager;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.datasource.DataSourceConfig;
import de.rytrox.varo.commands.CMDgamestate;
import de.rytrox.varo.listener.JoinAndQuitListener;
import de.rytrox.varo.utils.DiscordService;
import de.rytrox.varo.utils.GameStateHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.h2.Driver;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Varo extends JavaPlugin {

    private GameStateHandler gameStateHandler;
    private DiscordService discordService;

    private Database database;

    private TeamManager teamManager;
    private ScoreBoardManager scoreBoardManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        installDDL();
        saveDefaultConfig();

        this.teamManager = new TeamManager(this);
        this.scoreBoardManager = new ScoreBoardManager(this);
        this.gameStateHandler = new GameStateHandler();

        this.discordService = new DiscordService();
        this.discordService.writeMessage("Der Server wurde gestartet!", DiscordService.DiscordColor.CYAN);

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new JoinAndQuitListener(), this);

        this.getCommand("gamestate").setExecutor(new CMDgamestate());
        this.getCommand("message").setExecutor(new MessageCommand());
        this.getCommand("message").setAliases(Collections.singletonList("msg"));
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
                Team.class
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

    public GameStateHandler getGameStateHandler() {
        return gameStateHandler;
    }

    public DiscordService getDiscordService() {
        return discordService;
    }
}
