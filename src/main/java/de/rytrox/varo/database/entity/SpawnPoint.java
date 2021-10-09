package de.rytrox.varo.database.entity;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;

/**
 * Entity that represents a Spawn-Location of a TeamMember
 *
 * @author Timeout
 */
@Entity
@Table(name = "SpawnPoints")
public class SpawnPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "world", nullable = false)
    private String world;

    @Column(name = "x", nullable = false)
    private Double x;

    @Column(name = "y", nullable = false)
    private Double y;

    @Column(name = "z", nullable = false)
    private Double z;

    @Column(name = "yaw", nullable = false)
    private Float yaw;

    @Column(name = "pitch", nullable = false)
    private Float pitch;

    @OneToOne(targetEntity = TeamMember.class)
    @JoinColumn(name = "member")
    private TeamMember member;

    /**
     * Constructor for JPA
     */
    public SpawnPoint() {
    }

    public SpawnPoint(@NotNull Location location) {
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    @Nullable
    public Integer getId() {
        return id;
    }

    public void setId(@NotNull Integer id) {
        this.id = id;
    }

    public void setWorld(@NotNull String name) {
        this.world = name;
    }

    @NotNull
    public String getWorldName() {
        return world;
    }
    
    @NotNull
    public World getWorld() {
        return Bukkit.getWorld(this.world);
    }

    public void setX(@NotNull Double x) {
        this.x = x;
    }

    @NotNull
    public Double getX() {
        return this.x;
    }

    public void setY(@NotNull Double y) {
        this.y = y;
    }

    @NotNull
    public Double getY() {
        return this.y;
    }

    public void setZ(@NotNull Double z) {
        this.z = z;
    }

    @NotNull
    public Double getZ() {
        return this.z;
    }

    public void setYaw(@NotNull Float yaw) {
        this.yaw = yaw;
    }

    @NotNull
    public Float getYaw() {
        return this.yaw;
    }

    public void setPitch(@NotNull Float pitch) {
        this.pitch = pitch;
    }

    @NotNull
    public Float getPitch() {
        return this.pitch;
    }

    @NotNull
    public TeamMember getMember() {
        return member;
    }

    public void setMember(@NotNull TeamMember member) {
        this.member = member;
    }

    @NotNull
    public Location getLocation() {
        return new Location(getWorld(), x, y, z, yaw, pitch);
    }

    public void setLocation(@NotNull Location location) {
        this.world = location.getWorld().getName();

        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();

        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    @Override
    public String toString() {
        return "world: '" + world + '\'' +
                ", x: " + x +
                ", y: " + y +
                ", z: " + z +
                ", yaw: " + yaw +
                ", pitch: " + pitch;
    }
}
