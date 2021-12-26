package de.rytrox.varo.spawn_protection;

import de.rytrox.varo.Varo;
import de.rytrox.varo.gamestate.GameStateHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

public class SpawnProtectionListener implements Listener {

    private Set<Player> spawnProtection;
    private final Varo main;

    public SpawnProtectionListener(Varo main) {
        this.main = main;
        spawnProtection = new HashSet<>();
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if(spawnProtection.contains(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if(spawnProtection.contains(event.getEntity())
        || spawnProtection.contains(event.getDamager())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        spawnProtection.remove(event.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // give players invulnerability and paralysis after spawning
        GameStateHandler.GameState gameState = GameStateHandler.getInstance().getCurrentGameState();

        if(gameState == GameStateHandler.GameState.START
                || gameState == GameStateHandler.GameState.MAIN
                || gameState == GameStateHandler.GameState.FINAL) {
            Player player = event.getPlayer();
            Location spawnPoint = event.getPlayer().getLocation();

            spawnProtection.add(player);

            final BukkitTask[] task = new BukkitTask[1];

            task[0] = Bukkit.getScheduler().runTaskTimerAsynchronously(main, new Runnable() {

                private int counter = 10;

                @Override
                public void run() {

                    player.teleport(spawnPoint);

                    if (counter == 0) {
                        player.sendMessage(ChatColor.GREEN + "Deine Unverwundbarkeit und Paralyse sind nun aufgehoben!");
                        spawnProtection.remove(player);
                        task[0].cancel();
                        return;
                    }

                    player.sendMessage(String.format(ChatColor.YELLOW + "Du bist noch %d Sekunden unverwundbar und paralyisiert", counter));

                    counter--;
                }
            }, 0, 20);
        }
    }

}
