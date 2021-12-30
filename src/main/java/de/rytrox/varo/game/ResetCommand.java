package de.rytrox.varo.game;

import de.rytrox.varo.Varo;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class ResetCommand implements CommandExecutor {

    private final Varo main;
    private final List<CommandSender> confirmation;

    public ResetCommand(Varo main) {
        this.main = main;
        this.confirmation = new ArrayList<>();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if(commandSender.isOp()) {
            if(confirmation.contains(commandSender)
              && "confirm".equalsIgnoreCase(args[0])) {
                commandSender.sendMessage(ChatColor.GOLD + "Das Plugin wird zurückgesetzt und anschließend beendet. Reloade oder Restarte den Server, um das Plugin wieder zu aktivieren");
                confirmation.remove(commandSender);
                this.main.resetPlugin();
                return true;
            }

            commandSender.sendMessage(ChatColor.DARK_RED + "Durch einen reset wird die gesamte Datenbank zurückgesetzt. Bestätige durch die Eingabe von \"/reset confirm\"");
            confirmation.add(commandSender);
            return true;
        }
        commandSender.sendMessage(ChatColor.RED + "Nur Server-Operatoren dürfen diesen Befehl ausführen!");
        return true;
    }
}