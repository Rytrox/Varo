package de.rytrox.varo;

import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.commons.lang.reflect.MethodUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

public final class Varo extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        validateServerSettings();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void validateServerSettings() {
        boolean deactivatePlugin = false;

        if(this.getServer().getAllowEnd()) {
            getLogger().log(Level.INFO, "Das Ende ist noch aktiviert. Bitte deaktiviert es in der bukkit.yml unter settings.allow-end.");

            deactivatePlugin = true;
        }

        // TODO: Future Server-Setting Validation Here!

        if(deactivatePlugin) {
            getLogger().log(Level.INFO, "Deaktiviere Varo-Plugin");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }
}
