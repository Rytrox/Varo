package de.rytrox.varo.game.moderation;

import de.rytrox.varo.utils.CommandHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpectateCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player && sender.hasPermission("varo.admin.moderator")) {
            if(args.length > 0) {
                Player target = Bukkit.getPlayer(args[0]);

                if(target != null) {
                    if(!target.hasPermission("varo.admin.moderator")) {
                        ((Player) sender).teleport(target);

                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&6Varo&8] &7Du wurdest zum &2Spieler &a" + target.getDisplayName() + " &dteleportiert."));
                    } else sender.sendMessage(ChatColor.RED + "Moderatoren können nicht spectatet werden!");
                } else sender.sendMessage(ChatColor.RED + "Der Spieler " + args[0] + " ist nicht online!");
            } else sendHelp(sender);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(sender.hasPermission("varo.admin.moderator")) {
            return Bukkit.getOnlinePlayers()
                    .stream()
                    .filter(player -> !player.hasPermission("varo.admin.moderator"))
                    .map(Player::getName)
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(CommandHelper.formatCommandHeader("/spectate"));
        sender.sendMessage(CommandHelper.formatCommandExplanation("/spectate <player>", "Teleportier dich zu einen Spieler und beobachte diesen"));
    }
}
