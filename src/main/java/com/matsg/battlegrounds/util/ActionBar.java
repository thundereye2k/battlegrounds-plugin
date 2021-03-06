package com.matsg.battlegrounds.util;

import com.matsg.battlegrounds.BattlegroundsPlugin;
import com.matsg.battlegrounds.api.Battlegrounds;
import com.matsg.battlegrounds.api.util.Message;
import com.matsg.battlegrounds.api.util.Placeholder;
import com.matsg.battlegrounds.util.ReflectionUtils.EnumVersion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

public enum ActionBar implements Message {

    CHANGE_LOADOUT("actionbar-change-loadout"),
    LEAVE_ARENA("actionbar-leave-arena"),
    SAME_LOADOUT("actionbar-same-loadout");

    private Battlegrounds plugin;
    private EnumVersion version;
    private String message, path;

    ActionBar(String path) {
        this.plugin = BattlegroundsPlugin.getPlugin();
        this.message = plugin.getTranslator().getTranslation(path);
        this.path = path;
        this.version = ReflectionUtils.ENUM_VERSION;
    }

    public String getMessage() {
        return message;
    }

    public String getMessage(Placeholder... placeholders) {
        return replace(placeholders);
    }

    private String replace(Placeholder... placeholders) {
        return ChatColor.translateAlternateColorCodes('&', Placeholder.replace(message, placeholders));
    }

    public void send(Player player, Placeholder... placeholders) {
        String editMessage = replace(placeholders);

        try {
            Object actionBarPacket;

            if (version.getValue() < 12) {
                Object icbc = ReflectionUtils.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0]
                        .getMethod("a", String.class).invoke(null, "{\"text\":\"" + editMessage + "\"}");

                Constructor actionBarConstructor = ReflectionUtils.getNMSClass("PacketPlayOutChat")
                        .getConstructor(ReflectionUtils.getNMSClass("IChatBaseComponent"), byte.class);

                actionBarPacket = actionBarConstructor.newInstance(icbc, (byte) 2);
            } else {
                Object chatMessageType = ReflectionUtils.getNMSClass("ChatMessageType").getField("GAME_INFO").get(null);
                Object icbc = ReflectionUtils.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0]
                        .getMethod("a", String.class).invoke(null, "{\"text\":\"" + editMessage + "\"}");

                Constructor actionBarConstructor = ReflectionUtils.getNMSClass("PacketPlayOutChat")
                        .getConstructor(ReflectionUtils.getNMSClass("IChatBaseComponent"),
                                ReflectionUtils.getNMSClass("ChatMessageType"));

                actionBarPacket = actionBarConstructor.newInstance(icbc, chatMessageType);
            }

            ReflectionUtils.sendPacket(player, actionBarPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        return message + "@" + path;
    }
}