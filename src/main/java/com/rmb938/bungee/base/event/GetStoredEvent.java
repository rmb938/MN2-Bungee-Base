package com.rmb938.bungee.base.event;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class GetStoredEvent extends Event {

    private final ProxiedPlayer player;

    public GetStoredEvent(ProxiedPlayer player) {
        this.player = player;
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }
}
