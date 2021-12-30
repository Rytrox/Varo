package de.rytrox.varo.game;

import de.rytrox.varo.Varo;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.IOException;
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
                commandSender.sendMessage(ChatColor.GOLD + "Das Plugin wird zurückgesetzt und anschließend der Server reloadet");
                confirmation.remove(commandSender);
                try {
                    this.main.resetPlugin();
                } catch (IOException e) {
                    commandSender.sendMessage(ChatColor.RED + "Der Reset ist fehlgeschlagen");
                }
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
