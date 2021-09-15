package de.rytrox.varo.teams;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.Team;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.database.repository.TeamMemberRepository;
import de.rytrox.varo.database.repository.TeamRepository;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class TeamManager implements Listener {

    private final Varo main;

    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;

    public TeamManager(@NotNull Varo main) {
        this.main = main;
        this.teamMemberRepository = new TeamMemberRepository(main.getDatabase());
        this.teamRepository = new TeamRepository(main.getDatabase());

        Bukkit.getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void onRegisterPlayerInDatabase(@NotNull PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            // get player
            Player player = event.getPlayer();

            // get TeamMember-Object
            TeamMember member = teamMemberRepository.getPlayer(player);
            if(member == null) {
                // create a new member and save it
                member = new TeamMember();
                member.setTeam(null);
                System.out.println(player.getUniqueId());
                member.setUniqueID(player.getUniqueId());

                // save entity in Database
                main.getDatabase().save(member);
                main.getLogger().log(Level.INFO, "Saved Player {0} in database. It's his first start", player.getName());
            }
        });
    }

    /**
     * Creates a Team and inserts it into the Database <br>
     * Attention: This Method runs async
     *
     * @param executor the executor of the creation process
     * @param name the name of the Team
     */
    public void createTeam(CommandSender executor, String name) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            // check if team does not exist...
            if(teamRepository.findByName(name) == null) {
                // create a new Team
                Team team = new Team();
                team.setName(name);

                // and save it in database
                main.getDatabase().save(team);
                main.getLogger().log(Level.INFO, String.format("%s creates a new Team called %s", executor.getName(), name));
                executor.sendMessage(
                        ChatColor.translateAlternateColorCodes('&', String.format("&7Das Team &a%s &7wurde &aerfolgreich erstellt!", name))
                );
            } else executor.sendMessage(
                    ChatColor.translateAlternateColorCodes('&', "&cDieses Team existiert bereits"));
        });
    }
}
