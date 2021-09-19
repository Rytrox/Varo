package de.rytrox.varo.utils;

import de.rytrox.varo.Varo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DiscordService {

    final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd | HH:mm");
    static final String COORDINATE_TEMPLATE = "[x:%d | y:%d | z:%d]";

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

    public void writeMessage(String message) {
        writeMessage(message, DiscordColor.NONE, true);
    }

    public void writeMessage(String message, DiscordColor color) {
        writeMessage(message, color, true);
    }

    public void writeMessage(String message, boolean addTimestamp) {
        writeMessage(message, DiscordColor.NONE, addTimestamp);
    }

    public void writeMessage(String message, DiscordColor color, boolean addTimestamp) {

        StringBuilder sb = new StringBuilder();
        if(addTimestamp) {
            sb.append("Tag X : ");
            sb.append(timestampFormat.format(new Date()));
            sb.append("\n");
        }
        sb.append("```");
        sb.append(color.getKey());
        sb.append(message.replaceAll(ChatColor.COLOR_CHAR + "[0-9|a-f]", ""));
        sb.append("\n```");

        final String modifiedMessage = sb.toString();

        Bukkit.getConsoleSender().sendMessage("Sending message to discord: " + modifiedMessage);

        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(Varo.class), () -> {
            try {
                URL url = new URL("https://discord.com/api/webhooks/889044086579937281/KEd3kFA50XFavQCY0-kIp2Welwvb92evBlo4Lh8_Byi0mS92rVLgqwSqfQWq1iC76698");

                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.addRequestProperty("Content-Type", "application/json");
                connection.addRequestProperty("User-Agent", "MinecraftServer");
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");

                OutputStream stream = connection.getOutputStream();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("content", modifiedMessage);

                stream.write(jsonObject.toJSONString().getBytes());

                stream.flush();
                stream.close();

                connection.getInputStream().close();
                connection.disconnect();

            } catch (IOException ex) {
                Bukkit.getConsoleSender().sendMessage("ERROR - Discord-Nachricht konnte nicht gesendet werden");
                ex.printStackTrace();
            }
        });
    }

    public enum DiscordColor {
        NONE(""),
        BLACK("tex\n$ "),
        YELLOW("fix\n"),
        CYAN("yaml\n"),
        BLUE("md\n# "),
        RED("diff\n- ");

        private String key;

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

        String reason;

        CoordinateLeakReason(String reason) {
            this.reason = reason;
        }

        public String getReason() {
            return reason;
        }
    }

}
