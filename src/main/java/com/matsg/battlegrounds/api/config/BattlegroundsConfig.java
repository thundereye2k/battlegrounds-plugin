package com.matsg.battlegrounds.api.config;

import com.matsg.battlegrounds.api.Battlegrounds;
import com.matsg.battlegrounds.api.game.GameState;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BattlegroundsConfig extends AbstractYaml {

    public final boolean arenaProtection = getBoolean("game-arena-protection");
    public final boolean broadcastChat = getBoolean("game-broadcast-chat");
    public final boolean displayBloodEffect = getBoolean("game-display-blood-effect");
    public final ConfigurationSection lobbyScoreboard = getConfigurationSection("game-scoreboard.lobby");
    public final int explosiveCooldown = getInt("game-explosive-cooldown");
    public final int firearmDamageModifer = getInt("game-firearm-damage-modifier");
    public final int loadoutCreationLevel = getInt("loadout-creation-level");
    public final double firearmAccuracy = getDouble("game-firearm-accuracy");
    public final double launcherVelocity = getDouble("game-launcher-velocity");
    public final List<String> allowedCommands = getStringList("game-allowed-commands");
    public final List<String> joinableGamestates = getStringList("game-joinable-states");
    public final List<String> pierceableBlocks = getStringList("game-pierceable-blocks");
    public final String launcherMaterial = getString("game-launcher-material");

    public BattlegroundsConfig(Battlegrounds plugin) throws IOException {
        super(plugin, "config.yml", false);
        if (!getString("version").equals(plugin.getDescription().getVersion())) { //Auto update the config.yml when the plugin has updated
            removeFile();
            createFile(plugin.getDataFolder().getPath(), "config.yml");
        }
    }

    public String[] getGameSignLayout() {
        List<String> list = new ArrayList<>();
        for (String string : getConfigurationSection("game-sign-layout").getKeys(false)) {
            list.add(getString("game-sign-layout." + string));
        }
        return list.toArray(new String[list.size()]);
    }

    public String getGameSignState(GameState gameState) {
        return ChatColor.translateAlternateColorCodes('&', getString("game-sign-state." + gameState.toString().toLowerCase().replaceAll("_", "-")));
    }

    public Map<String, String> getLobbyScoreboardLayout() {
        Map<String, String> map = new HashMap<>();
        for (String string : getConfigurationSection("game-scoreboard.lobby.layout").getKeys(false)) {
            map.put(string, getString("game-scoreboard.lobby.layout." + string));
        }
        return map;
    }

    public String getWeaponDisplayName(String weaponType) {
        return getString("game-display-name." + weaponType);
    }
}