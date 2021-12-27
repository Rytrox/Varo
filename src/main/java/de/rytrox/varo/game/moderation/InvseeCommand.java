package de.rytrox.varo.game.moderation;

import de.rytrox.varo.Varo;
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

public class InvseeCommand implements TabExecutor {

    private final Varo main;

    public InvseeCommand(Varo main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player && main.getModeratorManager().isModerator(sender)) {
            if(args.length > 0) {
                Player target = Bukkit.getPlayer(args[0]);

                if(target != null) {
                    if(!main.getModeratorManager().isModerator(target)) {

                        ((Player)sender).openInventory(target.getInventory());

                    } else sender.sendMessage(ChatColor.RED + "Du kannst dir nicht das Inventar von Moderatoren ansehen");
                } else sender.sendMessage(ChatColor.RED + "Der Spieler " + args[0] + " ist nicht online!");
            } else sendHelp(sender);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(main.getModeratorManager().isModerator(sender)) {
            return Bukkit.getOnlinePlayers()
                    .stream()
                    .filter(player -> !main.getModeratorManager().isModerator(player))
                    .map(Player::getName)
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(CommandHelper.formatCommandHeader("/invsee"));
        sender.sendMessage(CommandHelper.formatCommandExplanation("/invsee <player>", "Sehe dir das Inventar eines Spielers an"));
    }
}
