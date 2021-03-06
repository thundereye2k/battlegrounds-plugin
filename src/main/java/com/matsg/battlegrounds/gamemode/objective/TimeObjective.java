package com.matsg.battlegrounds.gamemode.objective;

import com.matsg.battlegrounds.api.game.Game;
import com.matsg.battlegrounds.api.gamemode.Objective;
import com.matsg.battlegrounds.util.EnumTitle;
import com.matsg.battlegrounds.util.Title;

public class TimeObjective implements Objective {

    private int timeLimit;
    private String id;
    private Title title;

    public TimeObjective(int timeLimit) {
        this.timeLimit = timeLimit;
        this.id = "Time";
        this.title = EnumTitle.OBJECTIVE_TIME.getTitle();
    }

    public String getId() {
        return id;
    }

    public Title getTitle() {
        return title;
    }

    public boolean isReached(Game game) {
        return game.getTimeControl().getTime() >= timeLimit;
    }
}