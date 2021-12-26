package de.rytrox.varo.utils;

import de.rytrox.varo.Varo;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public class ConfigCreator {

    private final Varo main;
    private final File pluginDataFolder;

    /**
     * Creates a new ConfigCreator
     *
     * @param main the main class of the plugin
     */
    public ConfigCreator(@NotNull Varo main) {
        this.main = main;
        this.pluginDataFolder = main.getDataFolder();
    }

    /**
     * Copies a file and its content from the Java archive to the specified toPath location in the plugin's folder.
     * If the specified file cannot be found, a FileNotFoundException is thrown.
     *
     * @param fromPath the subpath of the config you want to load from
     * @param toPath   the subpath where the file should be copied
     * @return the file with the content inside the datafolder
     * @throws IOException              if your disk is unwritable or any path could not be found
     * @throws IllegalArgumentException if any argument is null
     */
    @NotNull
    public File copyDefaultFile(@NotNull Path fromPath, @NotNull Path toPath) throws IOException {
        // create new file if not exists
        File file = createFile(toPath);

        // try to get FileStream
        if (file.length() <= 2) {
            System.out.println(fromPath.toString());
            try (InputStream in = main.getResource(fromPath.toString());
                 FileWriter out = new FileWriter(file)) {
                // check if file was found
                if (in != null) {
                    IOUtils.copy(in, out, StandardCharsets.UTF_8);
                } else
                    Bukkit.getLogger().log(Level.WARNING, "&cUnable to copy content of {0} to Plugin folder", fromPath.getFileName());
            }
        }

        return file;
    }

    /**
     * Creates a new file inside the plugin's datafolder.
     *
     * @param filePath the subpath of the new file. Starting from the plugin's datafolder. Cannot be null
     * @return the created file
     * @throws IOException              if the disk is not writable
     * @throws IllegalArgumentException if the parameter is null
     */
    @NotNull
    public File createFile(@NotNull Path filePath) throws IOException {
        Path configFile = pluginDataFolder.toPath().resolve(filePath);

        // Create datafolder
        Files.createDirectories(configFile.getParent());

        if (!configFile.toFile().exists()) {
            Files.createFile(configFile);

            Bukkit.getLogger().log(Level.FINE, "&7Created new file {0} in datafolder", configFile.getFileName());
        }

        return configFile.toFile();
    }
}
