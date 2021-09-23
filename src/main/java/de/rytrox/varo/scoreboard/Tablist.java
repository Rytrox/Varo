package de.rytrox.varo.scoreboard;

import de.rytrox.varo.database.entity.Team;
import net.minecraft.server.v1_8_R3.Scoreboard;
import net.minecraft.server.v1_8_R3.ScoreboardTeam;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public final class Tablist extends Scoreboard {

    private static final Tablist INSTANCE = new Tablist();

    private final Set<Team> registeredTeams = new HashSet<>();

    public static Tablist getInstance() {
        return INSTANCE;
    }

    @Override
    public Collection<String> getTeamNames() {
        return registeredTeams
                .stream()
                .map(Team::getName)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ScoreboardTeam> getTeams() {
        return registeredTeams
                .stream()
                .map((team) -> new ScoreboardTeam(getInstance(), team.getName()))
                .collect(Collectors.toList());
    }

    public ScoreboardTeam getScoreboardTeam(@NotNull Team team) {
        ScoreboardTeam scoreboardTeam = new ScoreboardTeam(INSTANCE, team.getName());

        scoreboardTeam.setPrefix(Optional.ofNullable(team.getPrefix()).orElse(""));
        scoreboardTeam.setDisplayName(Optional.ofNullable(team.getDisplayName()).orElse(""));
        scoreboardTeam.getPlayerNameSet().addAll(team.getPlayerNameSet());

        return scoreboardTeam;
    }

    /**
     * Register a new team in this scoreboard
     *
     * @param team the team you want to register
     * @return true if the team was not registered before. False otherwise
     */
    public boolean registerTeam(@NotNull Team team) {
        return this.registeredTeams.add(team);
    }

    /**
     * Returns all registered teams
     *
     * @return a list containing all registered teams
     */
    @NotNull
    public List<Team> getRegisteredTeams() {
        return new ArrayList<>(registeredTeams);
    }
}
