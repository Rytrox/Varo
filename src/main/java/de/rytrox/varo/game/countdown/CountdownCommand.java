package de.rytrox.varo.game.countdown;

import com.google.gson.JsonObject;

import de.rytrox.varo.Varo;
import de.rytrox.varo.gamestate.GameStateHandler;
import de.rytrox.varo.utils.CommandHelper;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CountdownCommand implements TabExecutor {

    private final List<ChatColor> COLORS = Arrays.asList(
            ChatColor.DARK_RED,
            ChatColor.RED,
            ChatColor.GOLD,
            ChatColor.YELLOW,
            ChatColor.DARK_GREEN,
            ChatColor.GREEN
    );

    private final List<Sound> SOUNDS = Arrays.asList(
            Sound.ANVIL_LAND,
            Sound.ENDERMAN_TELEPORT,
            Sound.CAT_PURREOW,
            Sound.CLICK,
            Sound.NOTE_PIANO,
            Sound.ORB_PICKUP
    );

    private final GameStateHandler gameStateHandler;
    private final AtomicInteger counter = new AtomicInteger(60);
    private final Varo main;

    private BukkitTask countingRunner;

    public CountdownCommand(@NotNull Varo main) {
        this.main = main;
        this.gameStateHandler = main.getGameStateHandler();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length > 0) {
            if("start".equalsIgnoreCase(args[0])) {
                if(sender.hasPermission("varo.countdown.start")) {
                    if(gameStateHandler.getCurrentGameState() == GameStateHandler.GameState.PRE_GAME) {
                        start();
                    } else sender.sendMessage(ChatColor.RED + "Um den Countdown zu starten, muss das Spiel in der Vorbereitungsphase sein");
                } else sender.sendMessage(ChatColor.RED + "Du hast nicht die benötigte Berechtigung, diesen Befehl auszuführen");

                return true;
            } else if("stop".equalsIgnoreCase(args[0])) {
                if(sender.hasPermission("varo.countdown.stop")) {
                    if(gameStateHandler.getCurrentGameState() == GameStateHandler.GameState.PRE_GAME) {
                        stop();
                    } else sender.sendMessage(ChatColor.RED + "Um den Countdown zu starten, muss das Spiel in der Vorbereitungsphase sein");
                } else sender.sendMessage(ChatColor.RED + "Du hast nicht die benötigte Berechtigung, diesen Befehl auszuführen");

                return true;
            }
        }

        sendHelp(sender);
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(CommandSender sender, Command command, String label, @NotNull String[] args) {
        if(args.length == 1) {
            return Arrays.asList("start", "stop");
        }

        return new ArrayList<>();
    }

    private void sendHelp(@NotNull CommandSender sender) {
        sender.sendMessage(CommandHelper.formatCommandHeader("/countdown"));
        sender.sendMessage(CommandHelper.formatCommandExplanation("/countdown start", "Startet den Countdown"));
        sender.sendMessage(CommandHelper.formatCommandExplanation("/countdown stop", "Stoppt den Countdown"));
    }

    public void start() {
        this.countingRunner = Bukkit.getScheduler().runTaskTimer(main, () -> {
            int current = this.counter.getAndDecrement();

            if(current == 0) {
                gameStateHandler.setCurrentGameState(GameStateHandler.GameState.MAIN);
                stop();

                sendStartMessage();
            } else if(current < 10 || current % 5 == 0) {
                Bukkit.getOnlinePlayers()
                        .forEach((player) -> sendCountdown(player, current));
            }
        }, 0, 20L);
    }

    public void stop() {
        this.countingRunner.cancel();

        this.countingRunner = null;
        this.counter.set(60);
    }

    private void sendStartMessage() {
        JsonObject json = new JsonObject();
        json.addProperty("text", ChatColor.GREEN + "Möget die Spiele beginnen!");

        IChatBaseComponent message = IChatBaseComponent.ChatSerializer.a(json.toString());
        PacketPlayOutTitle packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, message, 4, 12, 4);

        Bukkit.getOnlinePlayers().forEach((Player p) -> {
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);

            p.playSound(p.getLocation(), Sound.FIREWORK_LAUNCH, 1F, 1F);
        });
    }

    /**
     * Sends a string as a title to a certain player
     *
     * @param player the player that should receive the title
     * @param current the current time in seconds
     */
    private void sendCountdown(@NotNull Player player, int current) {
        CraftPlayer cp = (CraftPlayer) player;
        int index = getIndex(current);

        IChatBaseComponent message = IChatBaseComponent.ChatSerializer.a(COLORS.get(index) + String.valueOf(current));
        PacketPlayOutTitle textPacket =
                new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, message, 4, 12, 4);

        cp.getHandle().playerConnection.sendPacket(textPacket);
        player.playSound(player.getLocation(), SOUNDS.get(index), 1F, 1F);
    }

    private int getIndex(int time) {
        switch(time) {
            case 40:
            case 35:
            case 30:
                return 1;
            case 25:
            case 20:
                return 2;
            case 15:
            case 10:
            case 9:
            case 8:
            case 7:
                return 3;
            case 6:
            case 5:
            case 4:
                return 4;
            case 3:
            case 2:
            case 1:
                return 5;
            default:
                return 0;
        }
    }
}
