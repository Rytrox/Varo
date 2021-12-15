package de.rytrox.gamestate.timeout;

import org.jetbrains.annotations.NotNull;

/**
 * Interface that manages different GameStates
 *
 * Grundidee: Alles innerhalb der States soll der State selber regeln!
 * Wenn ich einen Listener für alle State brauche, mache ich den im onEnable des Plugins, ansonsten hier im OnEnable
 *
 * Vorteile:
 * - Bessere Übersicht
 * - Strukturiertes Arbeiten
 * - Keine unnötigen Überprüfungen welchen State wir haben
 *
 * Nachteile:
 * - Viele Klassen
 * - Zusammenarbeit von bei n Stati einen Listener für k < n Stati zu initialisieren, fehlt
 */
public interface GameState {

    void onEnable();

    void onDisable();

    @NotNull
    GameState nextState();
}
