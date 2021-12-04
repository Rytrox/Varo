package de.rytrox.varo.worldborder;

import de.rytrox.varo.Varo;
import de.rytrox.varo.utils.CommandHelper;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WorldBorderCommand implements CommandExecutor {

    private static final String PERMISSION = "varo.worldborder";
    private final Varo main;

    public WorldBorderCommand(Varo main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cDu bist nicht dazu berechtigt, diesen Command auszuführen"));
            return true;
        }

        if(args.length > 0) {
            if("set".equalsIgnoreCase(args[0])) {
                if(args.length > 1) {

                    // check if size is valid
                    double size = 0D;

                    try {
                        size = Double.parseDouble(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cDie angegebene Größe ist ungültig!"));
                        sendHelp(sender);
                    }

                    // set new worldborder size
                    main.getWorldBorderHandler().setSize(size, 1);
                    return true;
                }
            } else if("status".equalsIgnoreCase(args[0])) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        String.format("&eDie Worldborder hat momentan eine Größe von &6%.2f &eBlöcken",
                                this.main.getWorldBorderHandler().getSize())));

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&4SuddenDeath&e: " + (this.main.getWorldBorderHandler().isSuddenDeath() ? "&a[aktiv]" : "&c[inaktiv]")));

                return true;
            } else if("sd".equalsIgnoreCase(args[0]) || "suddendeath".equalsIgnoreCase(args[0])) {
                boolean result = main.getWorldBorderHandler().toggleSuddenDeath();

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        String.format("&4SuddenDeath &ewurde %s", result ? "&aaktiviert" : "&cdeaktiviert")));

                return true;
            }
        }

        sendHelp(sender);
        return true;
    }


    private void sendHelp(CommandSender sender) {
        sender.sendMessage(CommandHelper.formatCommandHeader("/varoworldborder"));
        sender.sendMessage(CommandHelper.formatCommandExplanation("/varoworldborder set <size>", "Setzt die Größe der Worldborder"));
        sender.sendMessage(CommandHelper.formatCommandExplanation("/varoworldborder status", "Gibt den aktuellen Status der Worldborder aus"));
        sender.sendMessage(CommandHelper.formatCommandExplanation("/varoworldborder suddendeath/sd", "Togglet den Sudden-Death-Modus der Worldborder"));
    }
}
