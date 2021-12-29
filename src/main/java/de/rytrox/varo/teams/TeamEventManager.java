package de.rytrox.varo.teams;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.database.repository.TeamMemberRepository;
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
import java.util.UUID;
import java.util.logging.Level;

public class TeamEventManager implements Listener {

    private final Varo main;

    private final TeamMemberRepository teamMemberRepository;
    private final Map<UUID, TeamMember> loadedTeamMember = new HashMap<>();

    public TeamEventManager(@NotNull Varo main) {
        this.main = main;
        this.teamMemberRepository = new TeamMemberRepository(main.getDB());
    }

    @EventHandler
    public void onRegisterPlayerInDatabase(@NotNull PlayerLoginEvent event) {
        final Player player = event.getPlayer();

        // Only include Players if they are not moderators
        if(!player.hasPermission("varo.admin.moderator")) {
            if(main.getGameStateHandler().getCurrentGameState() == GameStateHandler.GameState.SETUP ||
                    main.getGameStateHandler().getCurrentGameState() == GameStateHandler.GameState.PRE_GAME) {
                // get TeamMember-Object
                TeamMember member = teamMemberRepository.getPlayer(player);
                if(member == null) {
                    // create a new member and save it
                    member = new TeamMember();
                    member.setTeam(null);
                    member.setUniqueID(player.getUniqueId());

                    // save entity in Database
                    main.getDB().save(member);
                    main.getLogger().log(Level.INFO, "Saved Player {0} in database. It's his first start", player.getName());
                }

                TeamMemberLoginEvent spawnEvent = new TeamMemberLoginEvent(player, member);
                Bukkit.getPluginManager().callEvent(spawnEvent);
                if(spawnEvent.isCancelled()) {
                    event.disallow(PlayerLoginEvent.Result.KICK_BANNED, spawnEvent.getCancelMessage());
                } else {
                    this.loadedTeamMember.put(event.getPlayer().getUniqueId(), member);
                }
            } else event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST,
                    ChatColor.translateAlternateColorCodes('&',
                            "&8[&6Varo&8] &cEs ist nicht erlaubt, dass neue Spieler während eines Spiels den Server betreten um sich zu registrieren.")
                    );
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
