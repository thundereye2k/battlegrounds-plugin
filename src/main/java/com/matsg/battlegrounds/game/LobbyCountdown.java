package com.matsg.battlegrounds.game;

import com.matsg.battlegrounds.api.game.Countdown;
import com.matsg.battlegrounds.api.game.Game;
import com.matsg.battlegrounds.api.game.GameState;
import com.matsg.battlegrounds.api.util.Placeholder;
import com.matsg.battlegrounds.gui.scoreboard.LobbyScoreboard;
import com.matsg.battlegrounds.util.BattleRunnable;
import com.matsg.battlegrounds.util.BattleSound;
import com.matsg.battlegrounds.util.EnumMessage;

import java.util.ArrayList;
import java.util.List;

public class LobbyCountdown extends BattleRunnable implements Countdown {

    private boolean cancelled;
    private Game game;
    private int id, time;
    private List<Integer> display;
    private LobbyScoreboard scoreboard;

    public LobbyCountdown(Game game, int time, int... display) {
        this.game = game;
        this.time = time;
        this.cancelled = false;
        this.display = new ArrayList<>();
        this.scoreboard = new LobbyScoreboard(game);

        for (int i : display) {
            this.display.add(i);
        }
    }

    public void cancel() {
        plugin.getServer().getScheduler().cancelTask(id);
    }

    public void cancelCountdown() {
        cancelled = true;
        game.getPlayerManager().broadcastMessage(EnumMessage.COUNTDOWN_CANCELLED);
    }

    public void run() {
        id = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            int countdown = time;
            public void run() {
                if (cancelled) {
                    game.setState(GameState.WAITING);
                    game.updateSign();
                    cancel();
                    return;
                }
                if (countdown <= 0) {
                    game.startCountdown();
                    cancel();
                    return;
                }
                if (display.contains(countdown)) {
                    game.getPlayerManager().broadcastMessage(EnumMessage.COUNTDOWN_NOTE.getMessage(new Placeholder("bg_countdown", countdown)));
                    BattleSound.COUNTDOWN_NOTE.play(game);
                }
                scoreboard.setCountdown(countdown);
                scoreboard.display(game);
                countdown --;
            }
        }, 0, 20);
    }
}