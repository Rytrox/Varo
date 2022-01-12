package de.rytrox.varo.game;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.Team;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.database.enums.PlayerStatus;
import de.rytrox.varo.database.repository.TeamMemberRepository;
import de.rytrox.varo.database.repository.TeamRepository;
import de.rytrox.varo.gamestate.GameStateHandler;
import de.rytrox.varo.message.MessageService;
import de.rytrox.varo.teams.events.TeamMemberDeathEvent;
import de.rytrox.varo.teams.scoreboard.Tablist;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PlayerDeathListener implements Listener {

    private final Varo main;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MessageService messageService;

    public PlayerDeathListener(Varo main) {
        this.main = main;
        this.teamRepository = new TeamRepository(main.getDB());
        this.teamMemberRepository = new TeamMemberRepository(main.getDB());
        this.messageService = main.getMessageService();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Optional<TeamMember> teamMemberOptional = this.teamMemberRepository.findPlayerByUUID(event.getPlayer().getUniqueId());

        if(teamMemberOptional.isPresent()
                && teamMemberOptional.get().getSpawnPoint() != null) {
            event.setRespawnLocation(teamMemberOptional.get().getSpawnPoint().getLocation());
        }
    }

    @EventHandler
    public void onDeath(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player && (main.getGameStateHandler().getCurrentGameState() == GameStateHandler.GameState.MAIN ||
                main.getGameStateHandler().getCurrentGameState() == GameStateHandler.GameState.FINAL)) {
            Player entity = (Player) event.getEntity();

            // check if player would die now
            if(entity.getHealth() - event.getFinalDamage() <= 0) {
                eliminatePlayer(entity, null);
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof Player && (main.getGameStateHandler().getCurrentGameState() == GameStateHandler.GameState.MAIN ||
                main.getGameStateHandler().getCurrentGameState() == GameStateHandler.GameState.FINAL)) {
            Player entity = (Player) event.getEntity();

            // check if player would die now
            if(entity.getHealth() - event.getFinalDamage() <= 0) {
                // try to get killer
                Player killer = null;
                if(!(event.getDamager() instanceof Player)) {
                    if(event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) {
                        killer = (Player) ((Projectile) event.getDamager()).getShooter();
                    }
                } else killer = (Player) event.getDamager();

                eliminatePlayer(entity, killer != null ? teamMemberRepository.getPlayer(killer) : null);
            }
        }
    }

    @EventHandler
    public void messagePlayerDeath(TeamMemberDeathEvent event) {
        if(event.getKiller() != null) {
            messageService.writeMessage(
                    String.format("&8[&6Varo&8] &7%s &7wurde von %s &cgetötet!",
                            Tablist.getInstance().getDisplayName(event.getPlayer()),
                            Tablist.getInstance().getTeamDisplayName(event.getKiller().getPlayer())),
                    MessageService.DiscordColor.YELLOW,
                    true);
        } else {
            // announce death
            messageService.writeMessage(
                    String.format("&8[&6Varo&8] &7%s ist gestorben!",
                            Tablist.getInstance().getDisplayName(event.getPlayer())),
                    MessageService.DiscordColor.YELLOW,
                    true);
        }

        // check if team has members left
        if(Objects.requireNonNull(event.getMember().getTeam()).getMembers().stream().noneMatch(m -> m.getStatus() == PlayerStatus.ALIVE)) {
            // announce elimination
            messageService.writeMessage(
                    String.format("&4Das Team %s &4ist ausgeschieden!",
                            event.getMember().getTeam().getDisplayName()),
                    MessageService.DiscordColor.RED,
                    true
            );

            // check if final or game over
            List<Team> runningTeams = teamRepository.getAllTeamsWithAliveMembers();

            // check for final
            if(runningTeams.size() == 2) {
                main.getGameStateHandler().setCurrentGameState(GameStateHandler.GameState.FINAL);

                // announce final
                messageService.writeMessage(
                        String.format("&6Das Finale zwischen Team %s &6und Team %s &6hat begonnen!",
                                runningTeams.get(0).getDisplayName(),
                                runningTeams.get(1).getDisplayName()),
                        MessageService.DiscordColor.YELLOW,
                        true
                );

                // check for game end
            } else if(runningTeams.size() == 1) {
                main.getGameStateHandler().setCurrentGameState(GameStateHandler.GameState.POST);

                // announce game end and winner
                messageService.writeMessage(
                        String.format("&aDas Finale ist vorbei!!! Das Team %s &ahat das Spiel gewonnen!",
                                runningTeams.get(0).getDisplayName()),
                        MessageService.DiscordColor.YELLOW,
                        true
                );
            }
        }
    }

    private void eliminatePlayer(@NotNull Player player, @Nullable TeamMember killer) {
        this.teamMemberRepository.findPlayerByUUID(player.getUniqueId())
                .ifPresent((member) -> {
                    // update player status
                    member.setStatus(PlayerStatus.DEAD);
                    main.getDB().update(member);

                    // Fake drop of stuff
                    player.setHealth(20D);
                    player.setFoodLevel(20);
                    Arrays.stream(player.getInventory()
                            .getContents())
                            .filter(Objects::nonNull)
                            .forEach(itemStack -> player.getWorld().dropItemNaturally(player.getLocation(), itemStack));
                    player.getInventory().clear();

                    // Call event
                    Bukkit.getPluginManager().callEvent(new TeamMemberDeathEvent(member, killer));
                });
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        // remove default death message
        event.setDeathMessage(null);
    }

}
