package de.rytrox.varo.database.util;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;

import de.rytrox.varo.Varo;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class SQLiteDDLGenerator extends DdlGenerator {

    private final Varo main;

    public SQLiteDDLGenerator(Varo varo, SpiEbeanServer server, DatabasePlatform dbPlatform, ServerConfig serverConfig) {
        super(server, dbPlatform, serverConfig);

        this.main = varo;
    }

    @Override
    public String generateDropDdl() {
        try(InputStream in = main.getResource("dropTables.sql")) {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            main.getLogger().log(Level.SEVERE, "Unable to read createTables.sql", e);
        }

        return null;
    }

    @Override
    public String generateCreateDdl() {
        try(InputStream in = main.getResource("createTables.sql")) {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            main.getLogger().log(Level.SEVERE, "Unable to read createTables.sql", e);
        }

        return null;    }
}
