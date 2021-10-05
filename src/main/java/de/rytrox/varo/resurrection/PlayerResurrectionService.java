package de.rytrox.varo.resurrection;

import de.rytrox.varo.Varo;
import de.rytrox.varo.database.entity.TeamMember;
import de.rytrox.varo.database.enums.PlayerStatus;
import de.rytrox.varo.database.repository.TeamMemberRepository;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Skull;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PlayerResurrectionService {

    private static final List<Integer> validFlowerIds = Arrays.asList(37, 38);

    private PlayerResurrectionService() {}

    /**
     * Evaluates the destination of an item drop
     * @param item the item you want to evaluate the destination of
     * @return the destination of the item drop or null if it could not have been evaluated
     */
    public static DropDestination evaluateDropDestination(Item item) {
        // get initial position and velocity
        Location loc0 = item.getLocation();
        // "* 20" in order for the unit to be blocks/seconds instead of blocks/ticks
        Vector v0 = item.getVelocity().multiply(20);

        // get x-, y- and z-part of velocity
        double vx0 = v0.getX();
        double vy0 = v0.getY();
        double vz0 = v0.getZ();

        // iterate through time in 1/10 s steps
        for(int i = 1; i <= 10*100; i++) {
            double t = i/10.0D;

            // calculate location for the current time
            Location loc = new Location(loc0.getWorld(),
                    vx0*t + loc0.getX(),
                    (-0.5) * 16D * t * t + vy0 * t + loc0.getY(),
                    vz0 * t + loc0.getZ());

            // if block type is other than air => return current location as destination
            if(loc.getBlock().getType() != Material.AIR) {
                return new DropDestination(loc, t);
            }
        }

        return null;
    }

    /**
     * Checks if there is any flower around or at the specified location
     * @param searchCenter the center for the search
     * @return the location of the flower (null if there is no)
     */
    public static Location checkForFlower(Location searchCenter) {

        searchCenter.setX(searchCenter.getBlockX() + 0.5);
        searchCenter.setZ(searchCenter.getBlockZ() + 0.5);

        for(int dx = -1; dx <= 1; dx++) {
            for(int dy = 0; dy <= 1; dy++) {
                for(int dz = -1; dz <= 1; dz++) {
                    if(validFlowerIds.contains(searchCenter.clone().add(dx, dy, dz).getBlock().getTypeId())) {
                        return searchCenter.clone().add(dx, dy, dz);
                    }
                }
            }
        }

        return null;
    }

    /**
     * Checks whether the resurrection scheme has been build correctly
     * @param flowerLocation the location of the flower
     * @return the result of the check
     */
    public static boolean checkScheme(Location flowerLocation) {

        /* 0: sign; -: no sign
            - - -  |  - 0 -  |  - - -  |  - - -
            - - 0  |  - - -  |  0 - -  |  - - -
            - - -  |  - - -  |  - - -  |  - 0 -
         */
        boolean signIsPlaced = flowerLocation.clone().add(1, 0, 0).getBlock().getType() == Material.SIGN_POST
                            ^ flowerLocation.clone().add(0, 0, 1).getBlock().getType() == Material.SIGN_POST
                            ^ flowerLocation.clone().add(-1, 0, 0).getBlock().getType() == Material.SIGN_POST
                            ^ flowerLocation.clone().add(0, 0, -1).getBlock().getType() == Material.SIGN_POST;

        /* 0: valid positions; -: invalid positions
            0 - -  |  - - 0
            - - -  |  - - -
            - - 0  |  0 - -
         */
        boolean skullsArePlaced = (
                    flowerLocation.clone().add(-1, 0, -1).getBlock().getType() == Material.SKULL
                    && flowerLocation.clone().add(1, 0, 1).getBlock().getType() == Material.SKULL
                    && ((Skull) flowerLocation.clone().add(-1, 0, -1).getBlock().getState()).getSkullType() == SkullType.PLAYER
                    && ((Skull) flowerLocation.clone().add(1, 0, 1).getBlock().getState()).getSkullType() == SkullType.PLAYER
                )
                ^ (
                    flowerLocation.clone().add(-1, 0, 1).getBlock().getType() == Material.SKULL
                    && flowerLocation.clone().add(1, 0, -1).getBlock().getType() == Material.SKULL
                    && ((Skull) flowerLocation.clone().add(-1, 0, 1).getBlock().getState()).getSkullType() == SkullType.PLAYER
                    && ((Skull) flowerLocation.clone().add(1, 0, -1).getBlock().getState()).getSkullType() == SkullType.PLAYER
                );

        return signIsPlaced && skullsArePlaced;
    }

    /**
     * Checks if a specified player can resurrect their teammate
     * @param player the player who wants to resurrect their teammate
     * @return the teammate if they can be resurrected or null
     */
    public static TeamMember resurrectionIsPossibleABoolean(Player player) {

        Varo varo = JavaPlugin.getPlugin(Varo.class);
        TeamMemberRepository teamMemberRepository = new TeamMemberRepository(varo.getDB());

        TeamMember teamMember = teamMemberRepository.getPlayer(player);

        if(teamMember == null)
            return null;
        if(teamMember.getTeam() == null) {
            return null;
        }

        Optional<TeamMember> deadTeamMember = teamMember.getTeam().getMembers()
                .stream()
                .filter(member -> !member.getOfflinePlayer().getName().equalsIgnoreCase(player.getName()))
                .findFirst();

        if(!deadTeamMember.isPresent() || deadTeamMember.get().getStatus() != PlayerStatus.DEAD)
            return null;

        return deadTeamMember.get();
    }

    static class DropDestination {

        private final Location destination;
        private final double time;

        public DropDestination(Location destination, double time) {
            this.destination = destination;
            this.time = time;
        }

        public Location getDestination() {
            return destination;
        }

        public double getTime() {
            return time;
        }
    }

}
