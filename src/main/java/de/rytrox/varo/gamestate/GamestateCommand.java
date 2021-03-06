package de.rytrox.varo.gamestate;

import de.rytrox.varo.utils.CommandHelper;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Executes commands of form
 *      /gamestate set <gamestate>
 *      /gamestate next
 *      /gamestate list
 *      /gamestate status
 */
public class GamestateCommand implements TabExecutor {

    private final GameStateHandler gameStateHandler;

    public GamestateCommand(GameStateHandler gameStateHandler) {
        this.gameStateHandler = gameStateHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!sender.hasPermission("varo.gamestate")) {
            sender.sendMessage(ChatColor.RED + "Du hast nicht die notwendigen Berechtigungen um diesen Befehl ausführen zu können!");
            return true;
        }
        
        if(args.length > 0) {
            if("set".equalsIgnoreCase(args[0])) {

                if(args.length > 1) {
                    String demandedGameState = args[1];

                    Optional<GameStateHandler.GameState> foundGameState = Arrays.
                            stream(GameStateHandler.GameState.values()).
                            filter(gameState -> gameState.name().equalsIgnoreCase(demandedGameState))
                            .findAny();

                    if(foundGameState.isPresent()) {
                        GameStateHandler.GameState oldGameState = gameStateHandler.getCurrentGameState();
                        gameStateHandler.setCurrentGameState(foundGameState.get());

                        sender.sendMessage(String.format("Der GameState wurde von %s auf %s gewechselt",
                                oldGameState.name(),
                                gameStateHandler.getCurrentGameState().name()));

                    } else {

                        sender.sendMessage("Der angegebene GameState konnte nicht gefunden werden. Nutze /gamestate list um alle verfügbaren GameStates aufzulisten");
                    }
                    return true;
                }
            } else if("next".equalsIgnoreCase(args[0])) {
                GameStateHandler.GameState oldGameState = gameStateHandler.getCurrentGameState();
                gameStateHandler.nextGameState();

                sender.sendMessage(String.format("Der GameState wurde von %s auf %s gewechselt",
                        oldGameState.name(),
                        gameStateHandler.getCurrentGameState().name()));
                return true;
            } else if("list".equalsIgnoreCase(args[0])) {

                sender.sendMessage("GameStates:");
                for (GameStateHandler.GameState gameState : GameStateHandler.GameState.values()) {
                    sender.sendMessage(gameState.name());
                }
                return true;
            } else if("status".equalsIgnoreCase(args[0])) {

                sender.sendMessage("Aktueller GameState: " + gameStateHandler.getCurrentGameState().name());
                return true;
            }
        }

        sendHelp(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        if(args.length > 0 && "set".equalsIgnoreCase(args[0])) {
            return Arrays.stream(GameStateHandler.GameState.values()).map(Enum::name).collect(Collectors.toList());
        }

        return null;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(CommandHelper.formatCommandHeader("/gamestate"));
        sender.sendMessage(CommandHelper.formatCommandExplanation("/gamestate list", "Listet alle GameStates auf"));
        sender.sendMessage(CommandHelper.formatCommandExplanation("/gamestate status", "Gibt den aktuellen GameState aus"));
        sender.sendMessage(CommandHelper.formatCommandExplanation("/gamestate next", "Wechselt zum nächsten GameState"));
        sender.sendMessage(CommandHelper.formatCommandExplanation("/gamestate set <gamestate>", "Setzt einen spezifischen GameState"));
    }

}
