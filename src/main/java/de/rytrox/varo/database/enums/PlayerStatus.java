package de.rytrox.varo.database.enums;

import java.io.Serializable;

/**
 * Represents the Status of the Player.
 *
 * ALIVE are all Players that are registered and not dead. <br>
 * DEAD are all Players that are registered and dead.
 */
public enum PlayerStatus implements Serializable {

    ALIVE, DEAD
}
