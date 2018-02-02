package com.matsg.battlegrounds.api.item;

import com.matsg.battlegrounds.api.game.Game;
import com.matsg.battlegrounds.api.game.GamePlayer;
import org.bukkit.inventory.ItemStack;

public interface Item extends Cloneable, Comparable<Item> {

    Game getGame();

    GamePlayer getGamePlayer();

    Item clone();

    ItemSlot getItemSlot();

    ItemStack getItemStack();

    String getName();

    void onDrop();

    void onLeftClick();

    void onPickUp();

    void onRightClick();

    void onSwitch();

    void setGame(Game game);

    void setGamePlayer(GamePlayer gamePlayer);

    void setItemSlot(ItemSlot itemSlot);

    boolean update();
}