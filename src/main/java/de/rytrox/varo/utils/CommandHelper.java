package de.rytrox.varo.utils;

import org.bukkit.ChatColor;

/**
 * An Util-class for a unified command design
 */
public class CommandHelper {

    /**
     * Takes the command input and creates a corresponding header string
     * @param command The command you want to create the header for
     * @return The header String
     */
    public static String formatCommandHeader(String command) {
        return ChatColor.translateAlternateColorCodes('&',
                String.format("&7------- &8[&e%s&8]&7-------", command));
    }

    /**
     * Takes the command and its explanation and creates a corresponding help string
     * @param command The command you want to explain
     * @param explanation the explanation for the command
     * @return the help string
     */
    public static String formatCommandExplanation(String command, String explanation) {
        return ChatColor.translateAlternateColorCodes('&',
                String.format("&e%s &8- &7%s", command, explanation));
    }

}
