package de.rytrox.varo.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.regex.Pattern;

public class MojangAPI {

    private static final Pattern UUID_FIX = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

    @Contract("_ -> new")
    public static @NotNull CompletableFuture<OfflinePlayer> getOfflinePlayer(String username) {
        return CompletableFuture.supplyAsync(() -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(username);

            if(!player.hasPlayedBefore()) {
                // Do request uuid when a player has not been played before on this server
                try {
                    HttpsURLConnection url = (HttpsURLConnection)
                            new URL("https://api.mojang.com/users/profiles/minecraft/" + username).openConnection();
                    url.setRequestMethod("GET");
                    url.setDoOutput(true);
                    url.connect();

                    try(InputStreamReader reader = new InputStreamReader(url.getInputStream())) {
                        JsonObject object = new JsonParser().parse(reader).getAsJsonObject();

                        player = Bukkit.getOfflinePlayer(UUID.fromString(UUID_FIX.matcher(object.get("id").getAsString()
                                .replace("-", "")).replaceAll("$1-$2-$3-$4-$5")));
                    }
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
            }

            return player.getPlayer();
        });
    }
}
