package com.matsg.battlegrounds.gamemode;

import com.matsg.battlegrounds.api.Battlegrounds;
import com.matsg.battlegrounds.api.config.Yaml;
import com.matsg.battlegrounds.api.event.GamePlayerDeathEvent.DeathCause;
import com.matsg.battlegrounds.api.game.Game;
import com.matsg.battlegrounds.api.game.GameScoreboard;
import com.matsg.battlegrounds.api.game.Spawn;
import com.matsg.battlegrounds.api.game.Team;
import com.matsg.battlegrounds.api.item.Weapon;
import com.matsg.battlegrounds.api.player.GamePlayer;
import com.matsg.battlegrounds.api.player.Hitbox;
import com.matsg.battlegrounds.api.util.Placeholder;
import com.matsg.battlegrounds.game.BattleTeam;
import com.matsg.battlegrounds.gui.scoreboard.AbstractScoreboard;
import com.matsg.battlegrounds.gui.scoreboard.ScoreboardBuilder;
import com.matsg.battlegrounds.util.EnumMessage;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Map;
import java.util.Set;

public class CombatTraining extends AbstractGameMode {

    private boolean active, autoJoin;
    private Color color;
    private double minSpawnDistance;
    private GameScoreboard scoreboard;

    public CombatTraining(Battlegrounds plugin, Game game, Yaml yaml) {
        super(plugin, game, EnumMessage.CBT_NAME.getMessage(), EnumMessage.CBT_SHORT.getMessage(), yaml);
        this.active = false;
        this.autoJoin = yaml.getBoolean("auto-join");
        this.color = getConfigColor();
        this.minSpawnDistance = yaml.getDouble("minimum-spawn-distance");
        this.scoreboard = new CBTScoreboard(game);
    }

    public void onEnable() {
        active = true;
    }

    public void onDisable() {
        active = false;
    }

    public void addPlayer(GamePlayer gamePlayer) {
        if (getTeam(gamePlayer) != null) {
            return;
        }
        Team team = new BattleTeam(0, gamePlayer.getName(), color, ChatColor.WHITE);
        gamePlayer.setTeam(team);
        team.addPlayer(gamePlayer);
        teams.add(team);
    }

    private Color getConfigColor() {
        String[] array = yaml.getString("armor-color").split(",");
        return Color.fromRGB(Integer.parseInt(array[0]), Integer.parseInt(array[1]), Integer.parseInt(array[2]));
    }

    public Spawn getRespawnPoint(GamePlayer gamePlayer) {
        return game.getArena().getRandomSpawn(minSpawnDistance);
    }

    public GameScoreboard getScoreboard() {
        return scoreboard;
    }

    public void onDeath(GamePlayer gamePlayer, DeathCause deathCause) {
        game.getPlayerManager().broadcastMessage(Placeholder.replace(plugin.getTranslator().getTranslation(deathCause.getMessagePath()),
                new Placeholder("bg_player", gamePlayer.getName())));
    }

    public void onKill(GamePlayer gamePlayer, GamePlayer killer, Weapon weapon, Hitbox hitbox) {
        game.getPlayerManager().broadcastMessage(getKillMessage(hitbox).getMessage(new Placeholder[] {
                new Placeholder("bg_killer", killer.getName()),
                new Placeholder("bg_player", gamePlayer.getName()),
                new Placeholder("bg_weapon", weapon.getName())
        }));
    }

    public void removePlayer(GamePlayer gamePlayer) {
        gamePlayer.setTeam(null);
        Team team = getTeam(gamePlayer);
        if (team == null) {
            return;
        }
        teams.remove(team);
    }

    public void spawnPlayers(GamePlayer... players) {
        for (Team team : teams) {
            for (GamePlayer gamePlayer : team.getPlayers()) {
                Spawn spawn = game.getArena().getRandomSpawn();
                spawn.setGamePlayer(gamePlayer);
                gamePlayer.getPlayer().teleport(spawn.getLocation());
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!active || !autoJoin) {
            return;
        }
        game.getPlayerManager().addPlayer(event.getPlayer());
    }

    private class CBTScoreboard extends AbstractScoreboard {

        private CBTScoreboard(Game game) {
            this.game = game;
            this.scoreboardId = "cbt";

            layout.put("title", scoreboardId);
        }

        public String getScoreboardId() {
            return scoreboardId;
        }

        public Set<World> getWorlds() {
            return worlds;
        }

        public void addTeams(ScoreboardBuilder builder) {
            for (Team team : game.getGameMode().getTeams()) {
                org.bukkit.scoreboard.Team sbTeam = builder.addTeam(team.getName());
                scoreboardTeams.add(sbTeam);
                sbTeam.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
                for (GamePlayer gamePlayer : team.getPlayers()) {
                    sbTeam.addEntry(gamePlayer.getName());
                }
            }
        }

        public Scoreboard buildScoreboard(Map<String, String> layout, Scoreboard scoreboard, GamePlayer gamePlayer) {
            return scoreboard == null || scoreboard.getObjective(DisplaySlot.SIDEBAR) == null ||
                    !scoreboard.getObjective(DisplaySlot.SIDEBAR).getCriteria().equals(scoreboardId) ? getNewScoreboard(layout) : updateScoreboard(layout, scoreboard);
        }
    }
}