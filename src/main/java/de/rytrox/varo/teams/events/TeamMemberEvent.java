package de.rytrox.varo.teams.events;

import de.rytrox.varo.database.entity.TeamMember;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract event that will be triggered when something happens with TeamMembers
 *
 * @author Timeout
 */
public abstract class TeamMemberEvent extends PlayerEvent {

    protected final TeamMember member;

    public TeamMemberEvent(@NotNull Player who, @NotNull TeamMember teamMember) {
        super(who);

        this.member = teamMember;
    }

    /**
     * Returns the TeamMember
     *
     * @return the TeamMember. Cannot be null
     */
    @NotNull
    public TeamMember getMember() {
        return member;
    }
}
