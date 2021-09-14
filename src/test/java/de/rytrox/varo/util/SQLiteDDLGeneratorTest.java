package de.rytrox.varo.util;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.google.common.io.Files;
import de.rytrox.varo.Varo;
import de.rytrox.varo.database.util.SQLiteDDLGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class SQLiteDDLGeneratorTest {

    @Mock
    private Varo main;

    @Mock
    private SpiEbeanServer mockDatabase;

    @Mock
    private DatabasePlatform platform;

    @Mock
    private ServerConfig serverConfig;

    @Test
    void shouldReadFileCorrectly() throws IOException {
        File createTables = Paths.get("src", "main", "resources", "createTables.sql").toFile();
        File dropTables = Paths.get("src", "main", "resources", "dropTables.sql").toFile();

        Mockito.doReturn(new FileInputStream(createTables)).when(main).getResource(Mockito.eq("createTables.sql"));
        Mockito.doReturn(new FileInputStream(dropTables)).when(main).getResource(Mockito.eq("dropTables.sql"));

        SQLiteDDLGenerator generator = new SQLiteDDLGenerator(main, mockDatabase, platform, serverConfig);

        assertEquals(Files.toString(createTables, StandardCharsets.UTF_8), generator.generateCreateDdl());
        assertEquals(Files.toString(dropTables, StandardCharsets.UTF_8), generator.generateDropDdl());
    }
}
