package de.rytrox.gamestate.sether;

import de.rytrox.varo.utils.CommandHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Executes commands of form
 *      /gamestate set <gamestate>
 *      /gamestate next
 *      /gamestate list
 *      /gamestate status
 */
public class GameStateCommand implements TabExecutor {

    private final JavaPlugin main;

    public GameStateCommand(JavaPlugin main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length > 0) {
            if("set".equalsIgnoreCase(args[0])) {

                if(args.length > 1) {
                    String demandedGameState = args[1];

                    Optional<String> foundGameState = GameStateHandler
                            .getInstance(main)
                            .getGameStateIdentifiers()
                            .stream()
                            .filter(gameState -> gameState.equalsIgnoreCase(demandedGameState))
                            .findAny();

                    if(foundGameState.isPresent()) {
                        GameStateHandler gameStateHandler = GameStateHandler.getInstance(main);

                        String oldGameState = gameStateHandler.getCurrentGameState();
                        gameStateHandler.setCurrentGameStateByIdentifier(foundGameState.get());

                        sender.sendMessage(String.format("Der GameState wurde von %s auf %s gewechselt",
                                oldGameState,
                                gameStateHandler.getCurrentGameState()));

                    } else {

                        sender.sendMessage("Der angegebene GameState konnte nicht gefunden werden. Nutze /gamestate list um alle verfügbaren GameStates aufzulisten");
                    }
                    return true;
                }
            } else if("next".equalsIgnoreCase(args[0])) {

                GameStateHandler gameStateHandler = GameStateHandler.getInstance(main);

                String oldGameState = gameStateHandler.getCurrentGameState();
                gameStateHandler.nextGameState();

                sender.sendMessage(String.format("Der GameState wurde von %s auf %s gewechselt",
                        oldGameState,
                        gameStateHandler.getCurrentGameState()));
                return true;
            } else if("list".equalsIgnoreCase(args[0])) {

                sender.sendMessage("GameStates:");
                GameStateHandler.getInstance(main).getGameStateIdentifiers().forEach(sender::sendMessage);

                return true;
            } else if("status".equalsIgnoreCase(args[0])) {

                sender.sendMessage("Aktueller GameState: " + GameStateHandler.getInstance(main).getCurrentGameState());
                return true;
            }
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(CommandHelper.formatCommandHeader("/gamestate"));
        sender.sendMessage(CommandHelper.formatCommandExplanation("/gamestate list", "Listet alle GameStates auf"));
        sender.sendMessage(CommandHelper.formatCommandExplanation("/gamestate status", "Gibt den aktuellen GameState aus"));
        sender.sendMessage(CommandHelper.formatCommandExplanation("/gamestate next", "Wechselt zum nächsten GameState"));
        sender.sendMessage(CommandHelper.formatCommandExplanation("/gamestate set <gamestate>", "Setzt einen spezifischen GameState"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(args.length > 0 && "set".equalsIgnoreCase(args[0])) {
            return GameStateHandler.getInstance(main).getGameStateIdentifiers();
        }

        return new ArrayList<>();
    }
}
