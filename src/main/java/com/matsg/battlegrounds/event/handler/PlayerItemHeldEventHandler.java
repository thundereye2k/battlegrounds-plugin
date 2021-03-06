package com.matsg.battlegrounds.event.handler;

import com.matsg.battlegrounds.api.event.handler.EventHandler;
import com.matsg.battlegrounds.api.game.Game;
import com.matsg.battlegrounds.api.item.Weapon;
import com.matsg.battlegrounds.api.player.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemHeldEvent;

public class PlayerItemHeldEventHandler implements EventHandler<PlayerItemHeldEvent> {

    private Game game;

    public PlayerItemHeldEventHandler(Game game) {
        this.game = game;
    }

    public boolean handle(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = game.getPlayerManager().getGamePlayer(player);

        if (gamePlayer == null || gamePlayer.getLoadout() == null) {
            return false;
        }

        Weapon weapon = gamePlayer.getLoadout().getWeapon(player.getItemInHand());

        if (weapon == null) {
            return false;
        }

        weapon.onSwitch(player);
        return false;
    }
}
