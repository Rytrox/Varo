package de.rytrox.varo.database.entity;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Item that is inside the Teams-Inventory
 *
 * @author Timeout
 */
@Entity
@Table(name = "TeamItems")
public class TeamItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "slot", nullable = false)
    private Integer slot;

    @ManyToOne(targetEntity = Team.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "owner", nullable = false)
    private Team owner;

    @Column(name = "item")
    private String item;

    /**
     * get the slot of the item in the inventory
     *
     * @return the slot if the item
     */
    @NotNull
    public Integer getSlot() {
        return slot;
    }

    /**
     * sets the slot of the item in the inventory
     *
     * @param slot the slot of the item
     */
    public void setSlot(@NotNull Integer slot) {
        this.slot = slot;
    }

    /**
     * Returns the ID of the Item
     *
     * @return the id of the item
     */
    @NotNull
    public Integer getId() {
        return id;
    }

    /**
     * Sets the ID of the Item
     *
     * @param id the ID of the item
     */
    public void setId(@NotNull Integer id) {
        this.id = id;
    }

    /**
     * Returns the Team that owns this item
     *
     * @return the owner of the item
     */
    @NotNull
    public Team getOwner() {
        return owner;
    }

    /**
     * Sets the owner of the Team
     *
     * @param team the owner of the Team
     */
    public void setOwner(@NotNull Team team) {
        this.owner = team;
    }

    @ApiStatus.Internal
    public @Nullable String getItem() {
        return item;
    }

    @ApiStatus.Internal
    public void setItem(@Nullable String item) {
        this.item = item;
    }

    /**
     * Converts the Base64-String and returns the ItemStack
     *
     * @return the ItemStack
     * @throws IOException if the ByteArray cannot be created
     * @throws ClassNotFoundException if the ItemStack-Class could not be found
     */
    @Nullable
    public ItemStack getItemStack() throws IOException, ClassNotFoundException {
        if(item != null) {
            try(ByteArrayInputStream str = new ByteArrayInputStream(Base64.getDecoder().decode(item));
                BukkitObjectInputStream data = new BukkitObjectInputStream(str)) {

                return (ItemStack) data.readObject();
            }
        }

        return null;
    }

    /**
     * Sets the Item of this Object
     *
     * @param item the new item of this object
     * @throws IOException if the ObjectStream could not be created
     */
    public void setItemStack(@NotNull ItemStack item) throws IOException {
        try(ByteArrayOutputStream str = new ByteArrayOutputStream();
            BukkitObjectOutputStream data = new BukkitObjectOutputStream(str)) {
            data.writeObject(item);

            this.item = Base64.getEncoder().encodeToString(str.toByteArray());
        }
    }
}
