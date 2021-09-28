package de.rytrox.varo.commands;

import de.rytrox.varo.utils.CommandHelper;
import de.rytrox.varo.utils.GameStateHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Optional;

/**
 * Executes commands of form
 *      /gamestate set <gamestate>
 *      /gamestate next
 *      /gamestate list
 *      /gamestate status
 */
public class CMDgamestate implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length > 0) {
            if("set".equalsIgnoreCase(args[0])) {

                if(args.length > 1) {
                    String demandedGameState = args[1];

                    Optional<GameStateHandler.GameState> foundGameState = Arrays.
                            stream(GameStateHandler.GameState.values()).
                            filter(gameState -> gameState.name().equalsIgnoreCase(demandedGameState))
                            .findAny();

                    if(foundGameState.isPresent()) {
                        GameStateHandler gameStateHandler = GameStateHandler.getInstance();

                        GameStateHandler.GameState oldGameState = gameStateHandler.getCurrentGameState();
                        gameStateHandler.setCurrentGameState(foundGameState.get());

                        sender.sendMessage(String.format("Der GameState wurde von %s auf %s gewechselt",
                                oldGameState.name(),
                                gameStateHandler.getCurrentGameState().name()));

                    } else {

                        sender.sendMessage("Der angegebene GameState konnte nicht gefunden werden. Nutze /gamestate list um alle verfügbaren GameStates aufzulisten");
                    }

                }

            } else if("next".equalsIgnoreCase(args[0])) {

                GameStateHandler gameStateHandler = GameStateHandler.getInstance();

                GameStateHandler.GameState oldGameState = gameStateHandler.getCurrentGameState();
                gameStateHandler.nextGameState();

                sender.sendMessage(String.format("Der GameState wurde von %s auf %s gewechselt",
                        oldGameState.name(),
                        gameStateHandler.getCurrentGameState().name()));

            } else if("list".equalsIgnoreCase(args[0])) {

                sender.sendMessage("GameStates:");
                for (GameStateHandler.GameState gameState : GameStateHandler.GameState.values()) {
                    sender.sendMessage(gameState.name());
                }
            } else if("status".equalsIgnoreCase(args[0])) {

                sender.sendMessage("Aktueller GameState: " + GameStateHandler.getInstance().getCurrentGameState().name());
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
}
