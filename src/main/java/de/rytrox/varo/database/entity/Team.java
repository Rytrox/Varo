package de.rytrox.varo.database.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Teams")
public class Team {

    @Id
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "displayname")
    private String displayName;

    @OneToMany(targetEntity = TeamMember.class, mappedBy = "team")
    private List<TeamMember> members;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TeamMember> getMembers() {
        return new ArrayList<>(members);
    }

    public void setMembers(List<TeamMember> members) {
        this.members = members;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
