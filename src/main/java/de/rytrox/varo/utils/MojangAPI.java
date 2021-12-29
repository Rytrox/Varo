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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.regex.Pattern;

public class MojangAPI {

    private static final Pattern UUID_FIX = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

    private static final Map<String, UUID> playerIDs = new HashMap<>();

    @Contract("_ -> new")
    public static @NotNull CompletableFuture<OfflinePlayer> getOfflinePlayer(String username) {
        return CompletableFuture.supplyAsync(() -> {
            // Bukkit saves OfflinePlayers in a weird way
            OfflinePlayer offlinePlayer;
            // first check if the player is currently cached
            UUID uuid = playerIDs.get(username.toLowerCase(Locale.ROOT));
            if(uuid == null) {
                // The user is not cached, try to get it from native CraftBukkit
                offlinePlayer = Bukkit.getOfflinePlayer(username);

                if(!offlinePlayer.hasPlayedBefore()) {
                    // Bukkit was not able to fetch the OfflinePlayer, use MojangAPI instead
                    try {
                        HttpsURLConnection url = (HttpsURLConnection)
                                new URL("https://api.mojang.com/users/profiles/minecraft/" + username).openConnection();
                        url.setRequestMethod("GET");
                        url.setDoOutput(true);
                        url.connect();

                        try(InputStreamReader reader = new InputStreamReader(url.getInputStream())) {
                            JsonObject object = new JsonParser().parse(reader).getAsJsonObject();

                            UUID correctID = UUID.fromString(UUID_FIX.matcher(object.get("id").getAsString()
                                    .replace("-", "")).replaceAll("$1-$2-$3-$4-$5"));
                            // cache MojangAPI-Result here!
                            playerIDs.put(object.get("name").getAsString().toLowerCase(Locale.ROOT), correctID);

                            // create OfflinePlayer with correct uuid
                            offlinePlayer = Bukkit.getOfflinePlayer(correctID);
                        }
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                }
            } else offlinePlayer = Bukkit.getOfflinePlayer(uuid);

            return offlinePlayer;
        });
    }
}
