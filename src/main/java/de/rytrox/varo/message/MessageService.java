package de.rytrox.varo.message;

import com.ibm.jvm.dtfjview.Output;
import de.rytrox.varo.Varo;
import de.rytrox.varo.gamestate.GameStateHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class MessageService {

    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd | HH:mm");
    private static final String COORDINATE_TEMPLATE = "[x:%d | y:%d | z:%d]";

    private final boolean discordEnabled;
    private final String discordWebhookURL;
    private final GameStateHandler gameStateHandler;

    public MessageService(@NotNull Varo main, @NotNull GameStateHandler gameStateHandler) {
        this.gameStateHandler = gameStateHandler;
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

        String messageBuilder = String.format("Koordinaten des Spielers %s:%n", player.getName()) +
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

                OutputStream[] stream = new OutputStream[] {null};
                HttpURLConnection connection[] = new HttpURLConnection[] {null};

                try (AutoCloseable closeable = () -> {
                    if(connection[0] != null) {
                        if(connection[0].getInputStream() != null){
                            connection[0].getInputStream().close();
                        }
                        connection[0].disconnect();
                    }
                    if(stream[0] != null) {
                        stream[0].close();
                    }
                }) {

                    connection[0] = (HttpURLConnection) new URL(discordWebhookURL).openConnection();

                    connection[0].addRequestProperty("Content-Type", "application/json");
                    connection[0].addRequestProperty("User-Agent", "MinecraftServer");
                    connection[0].setDoOutput(true);
                    connection[0].setRequestMethod("POST");

                    stream[0] = connection[0].getOutputStream();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("content", modifiedMessage);

                    stream[0].write(jsonObject.toJSONString().getBytes());
                    stream[0].flush();

                } catch (Exception ex) {
                    JavaPlugin.getPlugin(Varo.class).getLogger().log(Level.WARNING, "Discord-Nachricht konnte nicht gesendet werden");
                    ex.printStackTrace();
                }
            });
        }
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
