package de.rytrox.varo.database.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.*;

/**
 * Entity for Teams
 *
 * @author Timeout
 */
@Entity
@Table(name = "Teams")
public class Team {

    @Id
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "displayname")
    private String displayName;

    @OneToMany(targetEntity = TeamMember.class, mappedBy = "team", cascade = CascadeType.ALL)
    private Set<TeamMember> members;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Team team = (Team) o;

        return new EqualsBuilder().append(name, team.name).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(name).toHashCode();
    }

    /**
     * Returns the internal name of the Team. <br>
     * Cannot contain whitespaces
     *
     * @return the internal name of the Team
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Sets the internal name of the Team. <br>
     * Cannot contain whitespaces!
     *
     * @param name the new internal name of the team
     */
    public void setName(@NotNull String name) {
        this.name = name;
    }

    /**
     * Returns a List of all Members
     *
     * @return the list of all Members
     */
    public Set<TeamMember> getMembers() {
        return Optional.ofNullable(this.members)
                .map(HashSet::new)
                .orElse(null);
    }

    @ApiStatus.Internal
    public void setMembers(Set<TeamMember> members) {
        this.members = members;
    }

    /**
     * Returns the Displayname of the Team. <br>
     * ColorCode in correct format!
     *
     * @return the Displayname of the Team
     */
    @Nullable
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the Displayname of the Team. <br>
     * ColorCode in correct format!
     *
     * @param displayName the new displayname of the Team. ColorCode must be already formatted
     */
    public void setDisplayName(@Nullable String displayName) {
        this.displayName = displayName;
    }
}
