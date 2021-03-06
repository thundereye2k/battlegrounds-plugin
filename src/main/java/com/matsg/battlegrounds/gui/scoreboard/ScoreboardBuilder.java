package com.matsg.battlegrounds.gui.scoreboard;

import com.matsg.battlegrounds.api.util.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScoreboardBuilder {

    private int currentLine;
    private Scoreboard scoreboard;
    private Set<Objective> objectives;

    public ScoreboardBuilder() {
        this.currentLine = 0;
        this.objectives = new HashSet<>();
        this.scoreboard = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
    }

    public ScoreboardBuilder(Map<String, String> layout, Placeholder... placeholders) {
        this.currentLine = 0;
        this.objectives = new HashSet<>();
        this.scoreboard = Bukkit.getServer().getScoreboardManager().getNewScoreboard();

        addObjective(layout.get("id"), layout.get("title"), DisplaySlot.SIDEBAR);
        for (String line : layout.keySet()) {
            setLine(DisplaySlot.SIDEBAR, Integer.parseInt(line.substring(4, line.length())), Placeholder.replace(line, placeholders));
        }
    }

    public ScoreboardBuilder(Scoreboard scoreboard) {
        this.currentLine = 0;
        this.scoreboard = scoreboard;
        this.objectives = scoreboard.getObjectives();
    }

    public ScoreboardBuilder addLine(DisplaySlot slot, String... displayName) {
        for (String string : displayName) {
            getObjective(slot).getScore(string).setScore(currentLine);
            if (slot == DisplaySlot.SIDEBAR) {
                currentLine += 1;
            }
        }
        return this;
    }

    public ScoreboardBuilder addLine(DisplaySlot slot, int line, String displayName) {
        getObjective(slot).getScore(displayName).setScore(line);
        return this;
    }

    public ScoreboardBuilder addObjective(String id, String title, DisplaySlot slot) {
        if (getObjective(slot) != null) {
            return this;
        }
        Objective objective = scoreboard.registerNewObjective(title, id);
        objective.setDisplaySlot(slot);
        objectives.add(objective);
        return this;
    }

    public Team addTeam(String name) {
        Team team = scoreboard.getTeam(name);
        if (team != null) {
            team.unregister();
        }
        return this.scoreboard.registerNewTeam(name);
    }

    public Scoreboard build() {
        return scoreboard;
    }

    public ScoreboardBuilder clearLines() {
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }
        return this;
    }

    public int getLine(DisplaySlot slot, String displayName) {
        for (String entry : scoreboard.getEntries()) {
            if (entry.equals(displayName)) {
                return getObjective(slot).getScore(entry).getScore();
            }
        }
        return 0;
    }

    private Objective getObjective(DisplaySlot slot) {
        for (Objective objective : objectives) {
            if (objective.getDisplaySlot() == slot) {
                return objective;
            }
        }
        return null;
    }

    public Team getTeam(String name) {
        return scoreboard.getTeam(name);
    }

    public ScoreboardBuilder removeLine(DisplaySlot slot, int... lines) {
        for (String entry : scoreboard.getEntries()) {
            for (int line : lines) {
                if (getObjective(slot).getScore(entry).getScore() == line) {
                    scoreboard.resetScores(entry);
                }
            }
        }
        return this;
    }

    public ScoreboardBuilder setLine(DisplaySlot slot, int line, String displayName) {
        for (String entry : scoreboard.getEntries()) {
            if (getObjective(slot).getScore(entry).getScore() == line) {
                scoreboard.resetScores(entry);
                getObjective(slot).getScore(displayName).setScore(line);
                return this;
            }
        }
        getObjective(slot).getScore(displayName).setScore(line);
        return this;
    }

    public ScoreboardBuilder setTitle(DisplaySlot slot, String title) {
        getObjective(slot).setDisplayName(title);
        return this;
    }
}
