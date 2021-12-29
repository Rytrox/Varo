package de.rytrox.varo.message;

import com.google.gson.JsonObject;

import de.rytrox.varo.Varo;
import de.rytrox.varo.gamestate.GameStateHandler;
import de.rytrox.varo.teams.scoreboard.Tablist;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class MessageService {

    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd | HH:mm");
    private static final String COORDINATE_TEMPLATE = "[x:%d | y:%d | z:%d]";

    private final boolean discordEnabled;
    private final Varo main;
    private final String discordWebhookURL;
    private final GameStateHandler gameStateHandler;

    public MessageService(@NotNull Varo main) {
        this.main = main;
        this.gameStateHandler = main.getGameStateHandler();
        this.discordWebhookURL = main.getConfig().getString("discord.webhook", "");
        boolean discordEnabled = main.getConfig().getBoolean("discord.enabled", true);

        // warn if discord support is enabled, but no webhook url is given
        if(discordEnabled && discordWebhookURL.isEmpty()) {
            main.getLogger().log(Level.WARNING, "Du hast Discord in deiner Konfiguration aktiviert, aber keine Webhook-URL angegeben!");
        }

        this.discordEnabled = discordEnabled && !discordWebhookURL.isEmpty();
    }

    /**
     * Leaks a players location in the discord log
     * @param player the player you want to leak its coordinates from
     * @param reason the reason why the coordinates are being leaked
     */
    public void leakPlayerCoordinates(Player player, CoordinateLeakReason reason) {

        String messageBuilder = String.format("&4Koordinaten des Spielers %s&4:%n", Tablist.getInstance().getPrefix(player) + player.getName()) +
                String.format(
                        COORDINATE_TEMPLATE,
                        player.getLocation().getBlockX(),
                        player.getLocation().getBlockY(),
                        player.getLocation().getBlockZ()) +
                "\n" +
                reason.getReason();

        writeMessage(messageBuilder, DiscordColor.RED);
    }

    /**
     * Prints a Discord message
     * @param message The message
     */
    public void writeMessage(String message) {
        writeMessage(message, DiscordColor.NONE, true);
    }

    /**
     * Prints a Discord message
     * @param message The message
     * @param color The color of the message
     */
    public void writeMessage(String message, DiscordColor color) {
        writeMessage(message, color, true);
    }

    /**
     * Prints a Discord message
     * @param message The message
     * @param addTimestamp determines whether to add a timestamp
     */
    public void writeMessage(String message, boolean addTimestamp) {
        writeMessage(message, DiscordColor.NONE, addTimestamp);
    }

    /**
     * Prints a Discord message
     * @param message The message
     * @param color The color of the message
     * @param addTimestamp determines whether to add a timestamp
     */
    public void writeMessage(String message, DiscordColor color, boolean addTimestamp) {

        // don't send discord messages, as long as the game is in setup state
        if(gameStateHandler.getCurrentGameState() == GameStateHandler.GameState.SETUP)
            return;

        // broadcast message on minecraft server
        Bukkit.getServer().broadcastMessage(color == DiscordColor.NONE ? message : ChatColor.translateAlternateColorCodes('&', "&" + color.chatColorEquivalent + message));

        // if discord option is enabled, do the discord related stuff
        if(discordEnabled) {

            StringBuilder sb = new StringBuilder();
            if(addTimestamp) {
                sb.append("Tag X : ");
                sb.append(TIMESTAMP_FORMAT.format(new Date()));
                sb.append("\n");
            }
            sb.append("```");
            sb.append(color.getKey());
            sb.append(message.replaceAll(ChatColor.COLOR_CHAR + "[0-9|a-f]", ""));
            sb.append("\n```");

            final String modifiedMessage = sb.toString();

            Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(Varo.class), () -> {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(discordWebhookURL).openConnection();

                    connection.addRequestProperty("Content-Type", "application/json");
                    connection.addRequestProperty("User-Agent", "MinecraftServer");
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");


                    try (AutoCloseable _closeable = connection::disconnect;
                         OutputStream stream = connection.getOutputStream()) {
                        JsonObject object = new JsonObject();
                        object.addProperty("content", modifiedMessage);

                        stream.write(object.toString().getBytes());
                    } catch (Exception ex) {
                        main.getLogger().log(Level.WARNING, "Discord-Nachricht konnte nicht gesendet werden");
                    }
                } catch (IOException e) {
                    main.getLogger().log(Level.WARNING, "Unable to connect to discord-hook. Please check your URL and reload this plugin");
                }
            });
        }
    }

    public enum DiscordColor {
        NONE(""),
        BLACK("tex\n$ "),
        YELLOW("fix\n"),
        CYAN("yaml\n"),
        BLUE("md\n# "),
        RED("diff\n- ");

        private final String key;

        DiscordColor(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

    }


    public enum CoordinateLeakReason {

        CROSSTEAMING("Der Spieler wurde beim Crossteaming erwischt"),
        SPAWN_OUTSIDE_BORDER("Der Spieler ist außerhalb der Weltborder gespawnt und wurde nun zum Weltspawn teleportiert"),
        THREE_DAYS_RULE("Der Spieler hat seine drei Tage aufgebraucht");

        private final String reason;

        CoordinateLeakReason(String reason) {
            this.reason = reason;
        }

        public String getReason() {
            return reason;
        }
    }

}
