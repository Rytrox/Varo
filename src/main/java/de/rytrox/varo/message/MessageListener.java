package de.rytrox.varo.message;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.Team;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.database.enums.PlayerStatus;
import de.rytrox.varo.database.repository.TeamMemberRepository;
import de.rytrox.varo.game.moderation.ModeratorManager;
import de.rytrox.varo.gamestate.GameStateHandler;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MessageListener implements Listener {

    private static final String JOIN_MESSAGE = "%s &ahat den Server betreten";
    private static final String QUIT_MESSAGE = "%s &chat den Server verlassen";

    private final Varo main;
    private final TeamMemberRepository teamMemberRepository;

    public MessageListener(@NotNull Varo main) {
        this.main = main;
        this.teamMemberRepository = new TeamMemberRepository(main.getDB());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        GameStateHandler.GameState gameState = main.getGameStateHandler().getCurrentGameState();

        Optional<TeamMember> teamMemberOptional = teamMemberRepository.findPlayer(event.getPlayer());

        if(!ModeratorManager.isModerator(event.getPlayer())
            && teamMemberOptional.isPresent()
            && teamMemberOptional.get().getStatus() == PlayerStatus.ALIVE
            && gameState != GameStateHandler.GameState.POST) {

            main.getMessageService()
                    .writeMessage(String.format(JOIN_MESSAGE,
                                            main.getScoreBoardManager().getTablistName(event.getPlayer())),
                            MessageService.DiscordColor.CYAN
                    );
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        GameStateHandler.GameState gameState = main.getGameStateHandler().getCurrentGameState();

        Optional<TeamMember> teamMemberOptional = teamMemberRepository.findPlayer(event.getPlayer());

        if(!ModeratorManager.isModerator(event.getPlayer())
                && teamMemberOptional.isPresent()
                && teamMemberOptional.get().getStatus() == PlayerStatus.ALIVE
                && gameState != GameStateHandler.GameState.POST) {
            main.getMessageService()
                    .writeMessage(
                        String.format(QUIT_MESSAGE,
                                main.getScoreBoardManager().getTablistName(event.getPlayer())),
                        MessageService.DiscordColor.RED
                    );
        }
    }
}
