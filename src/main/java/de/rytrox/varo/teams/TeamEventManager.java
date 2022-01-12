package de.rytrox.varo.teams;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.database.repository.TeamMemberRepository;
import de.rytrox.varo.game.moderation.ModeratorManager;
import de.rytrox.varo.gamestate.GameStateHandler;
import de.rytrox.varo.teams.events.TeamMemberJoinEvent;
import de.rytrox.varo.teams.events.TeamMemberLoginEvent;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TeamEventManager implements Listener {

    private final TeamMemberRepository teamMemberRepository;
    private final Map<UUID, TeamMember> loadedTeamMember = new HashMap<>();
    private final Varo main;

    public TeamEventManager(@NotNull Varo main) {
        this.main = main;
        this.teamMemberRepository = new TeamMemberRepository(main.getDB());
    }

    @EventHandler
    public void onPlayerLogin(@NotNull PlayerLoginEvent event) {
        final Player player = event.getPlayer();

        // Only include Players if they are not moderators
        if(!ModeratorManager.isModerator(player)) {

            if(main.getGameStateHandler().getCurrentGameState() == GameStateHandler.GameState.SETUP) {
                event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, ChatColor.RED + "Der Server befindet sich in der Setup-Phase\nNur Moderatoren dürfen joinen!");
                return;
            }


            Optional<TeamMember> member = teamMemberRepository.findPlayer(player);

            // get TeamMember-Object
            if(!member.isPresent()) {
                event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST,
                        ChatColor.translateAlternateColorCodes('&',
                                "&8[&6Varo&8] &cDu wurdest weder von einem Moderator freigeschaltet, noch bist du ein Moderator")
                );

                return;
            }

            TeamMemberLoginEvent spawnEvent = new TeamMemberLoginEvent(member.get(), event.getPlayer());
            Bukkit.getPluginManager().callEvent(spawnEvent);
            if(spawnEvent.isCancelled()) {
                event.disallow(PlayerLoginEvent.Result.KICK_BANNED, spawnEvent.getCancelMessage());
            } else {
                this.loadedTeamMember.put(event.getPlayer().getUniqueId(), member.get());
            }
        }
    }

    @EventHandler
    public void onTeamMemberJoin(PlayerJoinEvent event) {
        TeamMember member = this.loadedTeamMember.remove(event.getPlayer().getUniqueId());

        if(member != null) {
            Bukkit.getPluginManager().callEvent(new TeamMemberJoinEvent(event.getPlayer(), member));
        }
    }
}
