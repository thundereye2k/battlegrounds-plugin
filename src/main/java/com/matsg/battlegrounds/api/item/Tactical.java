package com.matsg.battlegrounds.api.item;

public interface Tactical extends Equipment {

    Tactical clone();

    int getDuration();

    TacticalEffect getEffect();

    void setEffect(TacticalEffect effect);
}
