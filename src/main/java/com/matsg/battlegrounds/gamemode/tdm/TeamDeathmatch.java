package com.matsg.battlegrounds.gamemode.tdm;

import com.matsg.battlegrounds.api.config.Yaml;
import com.matsg.battlegrounds.api.event.GameEndEvent;
import com.matsg.battlegrounds.api.event.GamePlayerDeathEvent.DeathCause;
import com.matsg.battlegrounds.api.game.Game;
import com.matsg.battlegrounds.api.game.GameScoreboard;
import com.matsg.battlegrounds.api.game.Spawn;
import com.matsg.battlegrounds.api.game.Team;
import com.matsg.battlegrounds.api.gamemode.Objective;
import com.matsg.battlegrounds.api.item.Weapon;
import com.matsg.battlegrounds.api.player.GamePlayer;
import com.matsg.battlegrounds.api.player.Hitbox;
import com.matsg.battlegrounds.api.util.Placeholder;
import com.matsg.battlegrounds.game.BattleTeam;
import com.matsg.battlegrounds.gamemode.AbstractGameMode;
import com.matsg.battlegrounds.gamemode.Result;
import com.matsg.battlegrounds.gamemode.objective.EliminationObjective;
import com.matsg.battlegrounds.gamemode.objective.ScoreObjective;
import com.matsg.battlegrounds.gamemode.objective.TimeObjective;
import com.matsg.battlegrounds.util.EnumMessage;
import com.matsg.battlegrounds.util.EnumTitle;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class TeamDeathmatch extends AbstractGameMode {

    private boolean scoreboardEnabled;
    private double minSpawnDistance;
    private int killsToWin, lives;

    public TeamDeathmatch(Game game, Yaml yaml) {
        super(game, EnumMessage.TDM_NAME.getMessage(), EnumMessage.TDM_SHORT.getMessage(), yaml);
        this.killsToWin = yaml.getInt("kills-to-win");
        this.lives = yaml.getInt("lives");
        this.minSpawnDistance = yaml.getDouble("minimum-spawn-distance");
        this.scoreboardEnabled = yaml.getBoolean("scoreboard.enabled");
        this.teams.addAll(getConfigTeams());
        this.timeLimit = yaml.getInt("time-limit");

        objectives.add(new EliminationObjective());
        objectives.add(new ScoreObjective(killsToWin));
        objectives.add(new TimeObjective(timeLimit));
    }

    public void addPlayer(GamePlayer gamePlayer) {
        Team team = getEmptiestTeam();
        team.addPlayer(gamePlayer);
        gamePlayer.sendMessage(EnumMessage.TEAM_ASSIGNMENT.getMessage(new Placeholder("bg_team", team.getChatColor() + team.getName())));
    }

    private List<Team> getConfigTeams() {
        List<Team> list = new ArrayList<>();
        for (String teamId : yaml.getConfigurationSection("teams").getKeys(false)) {
            ConfigurationSection section = yaml.getConfigurationSection("teams." + teamId);
            String[] array = section.getString("armor-color").split(",");
            Color color = Color.fromRGB(Integer.parseInt(array[0]), Integer.parseInt(array[1]), Integer.parseInt(array[2]));

            list.add(new BattleTeam(Integer.parseInt(teamId), section.getString("name"), color, ChatColor.getByChar(section.getString("chatcolor").charAt(0))));
        }
        return list;
    }

    private Team getEmptiestTeam() {
        int size = Integer.MAX_VALUE;
        Team emptiestTeam = null;
        for (Team team : teams) {
            if (team.getTotalPlayers() < size) {
                emptiestTeam = team;
                size = team.getTotalPlayers();
            }
        }
        return emptiestTeam;
    }

    public Spawn getRespawnPoint(GamePlayer gamePlayer) {
        return game.getArena().getRandomSpawn(getTeam(gamePlayer), minSpawnDistance);
    }

    public GameScoreboard getScoreboard() {
        return scoreboardEnabled ? new TDMScoreboard(game, yaml) : null;
    }

    private Spawn getTeamBase(Team team) {
        for (Spawn spawn : game.getArena().getSpawns()) {
            if (spawn.getTeamId() == team.getId() && spawn.isTeamBase()) {
                return spawn;
            }
        }
        return null;
    }

    public void onDeath(GamePlayer gamePlayer, DeathCause deathCause) {
        game.getPlayerManager().broadcastMessage(Placeholder.replace(deathCause.getDeathMessage(), new Placeholder("bg_player", gamePlayer.getName())));
        handleDeath(gamePlayer);
    }

    public void onKill(GamePlayer gamePlayer, GamePlayer killer, Weapon weapon, Hitbox hitbox) {
        game.getPlayerManager().broadcastMessage(getKillMessage(hitbox).getMessage(new Placeholder[] {
                new Placeholder("bg_killer", getTeam(killer).getChatColor() + killer.getName() + ChatColor.WHITE),
                new Placeholder("bg_player", getTeam(gamePlayer).getChatColor() + gamePlayer.getName() + ChatColor.WHITE),
                new Placeholder("bg_weapon", weapon.getName())
        }));
        handleDeath(gamePlayer);
        killer.addExp(100);
        killer.setKills(killer.getKills() + 1);
        killer.getTeam().setScore(killer.getTeam().getScore() + 1);
        game.getPlayerManager().updateExpBar(killer);

        Objective objective = getReachedObjective();

        if (objective != null) {
            game.callEvent(new GameEndEvent(game, objective, getTopTeam(), getSortedTeams()));
            game.stop();
        }
    }

    public void onStart() {
        super.onStart();
        game.getPlayerManager().broadcastMessage(EnumTitle.TDM_START);
        for (GamePlayer gamePlayer : game.getPlayerManager().getPlayers()) {
            gamePlayer.setLives(lives);
        }
    }

    public void onStop() {
        Objective objective = getReachedObjective();

        for (Team team : teams) {
            Result result = Result.getResult(team, getSortedTeams());
            if (result != null) {
                for (GamePlayer gamePlayer : team.getPlayers()) {
                    objective.getTitle().send(gamePlayer.getPlayer(), new Placeholder("bg_result", result.getResultMessage()));
                }
            }
        }
    }

    public void removePlayer(GamePlayer gamePlayer) {
        Team team = getTeam(gamePlayer);
        if (team == null) {
            return;
        }
        team.removePlayer(gamePlayer);
    }

    public void spawnPlayers(GamePlayer... players) {
        for (GamePlayer gamePlayer : players) {
            Spawn spawn = getTeamBase(getTeam(gamePlayer));
            if (spawn == null) {
                spawn = game.getArena().getRandomSpawn(getTeam(gamePlayer));
            }
            gamePlayer.getPlayer().teleport(spawn.getLocation());
        }
    }
}