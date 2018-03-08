package com.matsg.battlegrounds.gamemode;

import com.matsg.battlegrounds.api.game.Game;
import com.matsg.battlegrounds.api.game.GameMode;
import com.matsg.battlegrounds.gamemode.ffa.FreeForAll;
import com.matsg.battlegrounds.gamemode.tdm.TeamDeathmatch;

public enum GameModeType {

    FREE_FOR_ALL(1) {
        public GameMode getInstance(Game game) {
            return new FreeForAll(game);
        }
    },
    TEAM_DEATHMATCH(2) {
        public GameMode getInstance(Game game) {
            return new TeamDeathmatch(game);
        }
    };

    private int id;

    GameModeType(int id) {
        this.id = id;
    }

    public static GameModeType valueOf(int id) {
        for (GameModeType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }

    public abstract GameMode getInstance(Game game);
}