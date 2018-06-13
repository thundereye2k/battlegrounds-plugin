package com.matsg.battlegrounds.util;

import com.matsg.battlegrounds.BattlegroundsPlugin;
import com.matsg.battlegrounds.api.Battlegrounds;
import com.matsg.battlegrounds.api.util.Message;
import com.matsg.battlegrounds.api.util.Placeholder;
import org.bukkit.entity.Player;

public enum EnumTitle implements Message {

    COUNTDOWN("title-countdown"),
    FFA_START("title-ffa-start"),
    TDM_START("title-tdm-start");

    private Battlegrounds plugin;
    private String path;
    private Title title;

    EnumTitle(String path) {
        this.plugin = BattlegroundsPlugin.getPlugin();
        if (path == null || path.length() <= 0) {
            throw new TitleFormatException("Title argument cannot be null");
        }
        String string = plugin.getTranslator().getTranslation(path);
        String[] split = string.split(",");
        if (split.length <= 4) {
            throw new TitleFormatException("Invalid title format \"" + string + "\"");
        }
        try {
            String title = split[0];
            String subTitle = split[1];
            int fadeIn = Integer.parseInt(split[2]);
            int time = Integer.parseInt(split[3]);
            int fadeOut = Integer.parseInt(split[4]);

            this.title = new Title(title, subTitle, fadeIn, time, fadeOut);
        } catch (Exception e) {
            throw new TitleFormatException("An error occurred while formatting the title");
        }
        this.path = path;
    }

    public String getMessage() {
        return title.getMessage();
    }

    public String getMessage(Placeholder... placeholders) {
        return title.getMessage(placeholders);
    }

    public void send(Player player, Placeholder... placeholders) {
        title.send(player, placeholders);
    }

    public String toString() {
        return title.toString() + "@" + path;
    }
}