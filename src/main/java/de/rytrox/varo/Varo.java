package de.rytrox.varo;

import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.SQLitePlatform;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import de.rytrox.varo.database.entity.Team;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.database.util.SQLiteDDLGenerator;
import de.rytrox.varo.teams.TeamManager;
import de.rytrox.varo.teams.TeamsCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.persistence.PersistenceException;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public final class Varo extends JavaPlugin {

    private TeamManager teamManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        setupDatabase();
        saveDefaultConfig();

        this.teamManager = new TeamManager(this);

        // register commands
        registerCommands();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void setupDatabase() {
        try {
            getDatabase().find(Team.class).findRowCount();
        } catch(PersistenceException e) {
            installDDL();
            getLogger().log(Level.INFO, "Initialize first usage for Varo");
        }
    }

    private void registerCommands() {
        this.getCommand("teams").setExecutor(new TeamsCommand(teamManager));
    }

    @Contract(pure = true)
    @Override
    public @NotNull List<Class<?>> getDatabaseClasses() {
        return Arrays.asList(
                TeamMember.class,
                Team.class
        );
    }

    @Override
    protected void installDDL() {
        SpiEbeanServer serv = (SpiEbeanServer)this.getDatabase();
        DdlGenerator gen = new SQLiteDDLGenerator(this, serv, serv.getDatabasePlatform(), getDatabaseConfig());
        gen.runScript(false, gen.generateCreateDdl());
    }

    @Override
    protected void removeDDL() {
        SpiEbeanServer serv = (SpiEbeanServer)this.getDatabase();
        DdlGenerator gen = new SQLiteDDLGenerator(this, serv, serv.getDatabasePlatform(), getDatabaseConfig());
        gen.runScript(true, gen.generateDropDdl());
    }

    @NotNull
    private ServerConfig getDatabaseConfig() {
        ServerConfig config = new ServerConfig();
        config.setDdlRun(true);
        config.setDdlGenerate(true);
        config.setClasses(getDatabaseClasses());
        config.setDatabasePlatform(new SQLitePlatform());

        DataSourceConfig sourceConfig = new DataSourceConfig();
        sourceConfig.setUsername("sa");
        sourceConfig.setPassword("");
        sourceConfig.setUrl(String.format("jdbc:sqlite:%s", new File(getDataFolder(), "Varo.db").getAbsolutePath()));
        sourceConfig.setDriver("org.sqlite.JDBC");
        config.setDataSourceConfig(sourceConfig);

        return config;
    }
}
