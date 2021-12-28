package de.rytrox.varo.teams;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReplyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        CommandSender lastConversationPartner = ConversationService.getLastConversationPartner(commandSender);

        if(lastConversationPartner == null) {
            commandSender.sendMessage(ChatColor.RED + "Du hast keine offene Konversation auf die du Antworten kannst");
        } else {
            // check if there is a message
            if(args.length < 1) {
                lastConversationPartner.sendMessage(ChatColor.RED + "/reply <message>");
                return true;
            }

            lastConversationPartner.sendMessage(
                ChatColor.translateAlternateColorCodes('&',
                    String.join(" ",
                        (String[]) ArrayUtils.subarray(args, 1, args.length)
                    )
                )
            );
            ConversationService.setLastConversationPartner(lastConversationPartner, commandSender);
        }
        return true;
    }
}
