package de.rytrox.varo.teams;

import de.rytrox.varo.Varo;
import de.rytrox.varo.utils.CommandHelper;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MessageCommand implements TabExecutor {

    private final Varo main;

    public MessageCommand(Varo main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length > 1) {
            Player target = Bukkit.getPlayer(args[0]);

            if(target != null) {
                if(!target.equals(commandSender)) {
                    String message = ChatColor.translateAlternateColorCodes('&', String.join(" ",
                            (String[]) ArrayUtils.subarray(args, 1, args.length)));

                    String fullMessage = ChatColor.translateAlternateColorCodes('&', "&8[&6MSG&8] &6") +
                            commandSender.getName() +
                            ChatColor.translateAlternateColorCodes('&', " &7➝ &6") +
                            target.getName() +
                            ChatColor.translateAlternateColorCodes('&', " &8» &7") +
                            message;

                    commandSender.sendMessage(fullMessage);
                    target.sendMessage(fullMessage);

                    Bukkit.getOnlinePlayers().forEach(p -> {
                        if(main.getModeratorManager().isModerator(p)) {
                            p.sendMessage(fullMessage);
                        }
                    });
                } else commandSender.sendMessage(ChatColor.RED + "Du kannst dir nicht selbst eine Nachricht verschicken.");
            } else commandSender.sendMessage(ChatColor.RED + "Der angegebene Spieler ist nicht online.");
        } else sendHelp(commandSender);


        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(CommandHelper.formatCommandHeader("/message oder /msg"));
        sender.sendMessage(CommandHelper.formatCommandExplanation("/msg", "Ruft die Hilfe auf"));
        sender.sendMessage(CommandHelper.formatCommandExplanation("/msg <player> <message>", "Sendet eine private Nachricht mit ColorCodes ('&') an einen Spieler, der online ist"));
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !main.getModeratorManager().isModerator(p))
                    .map(Player::getName)
                    .collect(Collectors.toList()), list);
        }

        return list;
    }
}
