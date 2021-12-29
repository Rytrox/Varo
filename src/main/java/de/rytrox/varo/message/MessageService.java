package de.rytrox.varo.message;

import de.rytrox.varo.Varo;
import de.rytrox.varo.gamestate.GameStateHandler;
import de.rytrox.varo.teams.scoreboard.Tablist;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class MessageService {

    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd | HH:mm");
    private static final String COORDINATE_TEMPLATE = "[x:%d | y:%d | z:%d]";

    private final Varo main;
    private final GameStateHandler gameStateHandler;

    public MessageService(@NotNull Varo main, @NotNull GameStateHandler gameStateHandler) {
        this.main = main;
        this.gameStateHandler = gameStateHandler;
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

        StringBuilder sb = new StringBuilder();
        if(addTimestamp) {
            sb.append("Tag ").append(main.getStateStorage().getInt("day", 0)).append(": ");
            sb.append(TIMESTAMP_FORMAT.format(new Date()));
            sb.append("\n");
        }
        sb.append("```");
        sb.append(color.getKey());
        sb.append(message.replaceAll(ChatColor.COLOR_CHAR + "[0-9|a-f]", ""));
        sb.append("\n```");

        final String modifiedMessage = sb.toString();

        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(Varo.class), () -> {

            HttpsURLConnection connection = null;
            OutputStream stream = null;

            try {
                URL url = new URL("https://discord.com/api/webhooks/925724164013326376/yNtTNG0LPAUudC63yAIXsIyTnfiZJRQh3tmz6lTLgZ2tRsYSgprfVr-TA831X58IXKZl");

                connection = (HttpsURLConnection) url.openConnection();
                connection.addRequestProperty("Content-Type", "application/json");
                connection.addRequestProperty("User-Agent", "MinecraftServer");
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");

                stream = connection.getOutputStream();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("content", modifiedMessage);

                stream.write(jsonObject.toJSONString().getBytes());
                stream.flush();

                Bukkit.getServer().broadcastMessage(color == DiscordColor.NONE ? message : ChatColor.translateAlternateColorCodes('&', "&" + color.chatColorEquivalent + message));

            } catch (IOException ex) {
                JavaPlugin.getPlugin(Varo.class).getLogger().log(Level.WARNING, "Discord-Nachricht konnte nicht gesendet werden");
                ex.printStackTrace();
            } finally {
                if(stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ignored) {}
                }
                if(connection != null) {
                    try {
                        connection.getInputStream().close();
                    } catch (IOException ignored) {}

                    connection.disconnect();
                }
            }
        });
    }

    public enum DiscordColor {
        NONE("", ' '),
        BLACK("tex\n$ ", '0'),
        YELLOW("fix\n", 'e'),
        CYAN("yaml\n", 'b'),
        BLUE("md\n# ", '9'),
        RED("diff\n- ", 'c');

        private final String key;
        private final char chatColorEquivalent;

        DiscordColor(String key, char chatColorEquivalent) {
            this.key = key;
            this.chatColorEquivalent = chatColorEquivalent;
        }

        public String getKey() {
            return key;
        }

        public char getChatColorEquivalent() {
            return chatColorEquivalent;
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
