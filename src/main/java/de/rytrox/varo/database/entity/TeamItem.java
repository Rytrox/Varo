package de.rytrox.varo.database.entity;

import org.bukkit.inventory.ItemStack;

import javax.persistence.*;

@Entity
@Table(name = "TeamItems")
public class TeamItem {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "slot")
    private Integer slot;

    @ManyToOne(targetEntity = Team.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "team")
    private Team team;

    @Transient
    @Column(name = "item")
    private ItemStack item;

    public Integer getSlot() {
        return slot;
    }

    public void setSlot(Integer slot) {
        this.slot = slot;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }
}
