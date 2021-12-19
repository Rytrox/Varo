package de.rytrox.varo.chat_log;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.ChatLog;
import de.rytrox.varo.utils.CommandHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChatLogCommand implements TabExecutor {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy-HH:mm");
    private final Varo main;

    public ChatLogCommand(Varo main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if(commandSender instanceof Player
            && main.getModeratorManager().isModerator(commandSender)) {

            Player moderator = (Player) commandSender;

            if(args.length >= 1) {

                String typeString = args[0];
                Optional<ChatLogType> chatLogTypeOptional = ChatLogType.fromName(typeString);

                if(!chatLogTypeOptional.isPresent()) {
                    commandSender.sendMessage(ChatColor.RED + "Ungültiger ChatLog-Typ");
                    sendHelp(commandSender);
                    return true;
                }

                ChatLogType chatLogType = chatLogTypeOptional.get();

                String[] players;

                Optional<String> lastOperator = Arrays.stream(args).filter("*last"::equalsIgnoreCase).findAny();
                Optional<String> betweenOperator = Arrays.stream(args).filter("*between"::equalsIgnoreCase).findAny();

                if(lastOperator.isPresent()) {
                    int indexOperator = Arrays.asList(args).indexOf(lastOperator.get());
                    players = Arrays.copyOfRange(args, 1, indexOperator);

                    if(args.length < indexOperator + 3) {
                        sendHelp(commandSender);
                        commandSender.sendMessage(ChatColor.RED + "Ungültige Zeitangabe");
                        return true;
                    }

                    String timeString = args[indexOperator + 1];
                    String unitString = args[indexOperator + 2];

                    LocalDateTime now = LocalDateTime.now();
                    long time;
                    ChronoUnit unit;


                    try {
                        time = Long.parseLong(timeString);
                    } catch (NumberFormatException e) {
                        sendHelp(commandSender);
                        commandSender.sendMessage(ChatColor.RED + "Ungültige Zeitangabe");
                        return true;
                    }

                    switch(unitString) {
                        case "d":
                        case "day":
                        case "days": unit = ChronoUnit.DAYS; break;
                        case "h":
                        case "hour":
                        case "hours": unit = ChronoUnit.HOURS; break;
                        case "m":
                        case "min":
                        case "minute":
                        case "minutes": unit = ChronoUnit.MINUTES; break;
                        default:
                            sendHelp(commandSender);
                            commandSender.sendMessage(ChatColor.RED + "Ungültige Zeiteinheit");
                            return true;
                    }

                    LocalDateTime from = now.minus(time, unit);

                    sendChatLog(commandSender, main.getChatLogRepository().getConversation(chatLogType, from, players));
                    return true;


                } else if(betweenOperator.isPresent()) {
                    int indexOperator = Arrays.asList(args).indexOf(betweenOperator.get());
                    players = Arrays.copyOfRange(args, 1, indexOperator);

                    if(args.length < indexOperator + 3) {
                        sendHelp(commandSender);
                        commandSender.sendMessage(ChatColor.RED + "Ungültige Zeitangabe");
                        return true;
                    }

                    String fromString = args[indexOperator + 1];
                    String toString = args[indexOperator + 2];
                    LocalDateTime from;
                    LocalDateTime to;

                    try {
                        from = LocalDateTime.parse(fromString, dateFormatter);
                        to = LocalDateTime.parse(toString, dateFormatter);
                    } catch (DateTimeParseException ignored) {
                        commandSender.sendMessage(ChatColor.RED + "Ungültiges Datum");
                        sendHelp(commandSender);
                        return true;
                    }

                    sendChatLog(commandSender, main.getChatLogRepository().getConversation(chatLogType, from, to, players));
                    return true;
                } else {
                    players = Arrays.copyOfRange(args, 1, args.length);
                    sendChatLog(commandSender, main.getChatLogRepository().getConversation(chatLogType, players));
                    return true;
                }
            }

            sendHelp(commandSender);
        }

        return true;
    }

    private void sendChatLog(CommandSender commandSender, List<ChatLog> chatLogs) {
        if(chatLogs.size() == 0) {
            commandSender.sendMessage(ChatColor.YELLOW + "Es existieren keine Chatlogs mit den angegebenen Eigenschaften");
        } else {
            chatLogs.forEach(chatLog -> commandSender.sendMessage(chatLog.toString()));
        }
    }

    private void sendHelp(CommandSender sender) {
        CommandHelper.sendCommandHeader("/chatlog", sender);
        CommandHelper.sendCommandExplanation("/chatlog <all, msg, global>", "Gibt alle Chatlogs des benannten Typs von allen Spielern aus", sender);
        CommandHelper.sendCommandExplanation("/chatlog <all, msg, global> <player>", "Gibt alle Chatlogs des benannten Typs eines Spielers aus", sender);
        CommandHelper.sendCommandExplanation("/chatlog <all, msg, global> <player1> <player2> <player3> ...", "Gibt die Chatlogs mehrerer Spieler aus", sender);
        CommandHelper.sendCommandExplanation("/chatlog <all, msg, global> <player> *last <number> <days/d, hours/h, minutes/m>", "Gibt alle Chatlogs der letzten <number> Tagen/Stunden/Minuten aus", sender);
        CommandHelper.sendCommandExplanation("/chatlog <all, msg, global> <player> *between <Datum im Format dd.MM.yyyy-HH:mm> <Datum im Format dd.MM.yyyy-HH:mm>", "Gibt alle Chatlogs der zwischen den angegebenen Daten aus", sender);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }
}
