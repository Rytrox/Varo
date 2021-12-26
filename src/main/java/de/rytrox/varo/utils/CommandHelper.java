package de.rytrox.varo.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * An Util-class for a unified command helping design
 */
public class CommandHelper {

    private CommandHelper() {}

    /**
     * Takes the command input and prints a corresponding header string
     * @param commandSender The command sender that needs help
     * @param command The command you want to create the header for
     */
    public static void sendCommandHeader(String command, CommandSender commandSender) {
        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                String.format("&7-------&8[&e%s&8]&7-------", command)));
    }

    /**
     * Takes the command and its explanation and prints a corresponding help string
     * @param command The command you want to explain
     * @param explanation the explanation for the command
     * @param commandSender The command sender that needs help
     */
    public static void sendCommandExplanation(String command, String explanation, CommandSender commandSender) {
        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                String.format("&e%s &8- &7%s", command, explanation)));
    }

    /**
     * Takes the command input and creates a corresponding header string
     * @param command The command you want to create the header for
     * @return The header String
     */
    @Deprecated
    public static String formatCommandHeader(String command) {
        return ChatColor.translateAlternateColorCodes('&',
                String.format("&7-------&8[&e%s&8]&7-------", command));
    }

    /**
     * Takes the command and its explanation and creates a corresponding help string
     * @param command The command you want to explain
     * @param explanation the explanation for the command
     * @return the help string
     */
    @Deprecated
    public static String formatCommandExplanation(String command, String explanation) {
        return ChatColor.translateAlternateColorCodes('&',
                String.format("&e%s &8- &7%s", command, explanation));
    }

}
