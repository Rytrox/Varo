package de.rytrox.varo.database.enums;

import java.io.Serializable;

/**
 * Represents the Status of the Player.
 *
 * ALIVE are all Players that are registered and not dead. <br>
 * DEAD are all Players that are registered and dead. <br>
 * NOT_REGISTERED are all Players that are not involved in the Game
 */
public enum PlayerStatus implements Serializable {

    ALIVE, DEAD, NOT_REGISTERED
}
