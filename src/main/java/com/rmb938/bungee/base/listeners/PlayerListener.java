package com.rmb938.bungee.base.listeners;

import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.bungee.base.entity.ExtendedServerInfo;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerListener implements Listener {

    private final MN2BungeeBase plugin;

    public PlayerListener(MN2BungeeBase plugin) {
        this.plugin = plugin;
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onPing(ProxyPingEvent event) {
        int totalPlayers = 0;
        for (ExtendedServerInfo extendedServerInfo : ExtendedServerInfo.getExtendedInfos().values()) {
            totalPlayers += extendedServerInfo.getCurrentPlayers();
        }
        ServerPing.PlayerInfo[] playerInfos = new ServerPing.PlayerInfo[plugin.getProxy().getConfig().getListeners().iterator().next().getMaxPlayers()];
        ServerPing.Players players = new ServerPing.Players(plugin.getProxy().getConfig().getListeners().iterator().next().getMaxPlayers(), totalPlayers, playerInfos);
        event.getResponse().setPlayers(players);

        if (plugin.isMaintenance()) {
            event.getResponse().setDescription("Maintenance Mode");
        }
    }

    @EventHandler
    public void onPlayerLogin(PreLoginEvent event) {
        if (plugin.isMaintenance()) {
            event.setCancelReason("Server is in Maintenance Mode");
            event.setCancelled(true);
        }
    }

}
