package com.rmb938.bungee.base.listeners;

import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.bungee.base.entity.ExtendedServerInfo;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PostLoginEvent;
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
        int max = 0;
        int online = 0;
        for (ExtendedServerInfo extendedServerInfo : ExtendedServerInfo.getExtendedInfos().values()) {
            max += extendedServerInfo.getMaxPlayers();
            online += extendedServerInfo.getCurrentPlayers();
        }
        ServerPing serverPing = new ServerPing();
        ServerPing.Players players = new ServerPing.Players(max, online, event.getResponse().getPlayers().getSample());
        serverPing.setPlayers(players);
        if (plugin.isMaintenance()) {
            serverPing.setDescription(plugin.getMainConfig().maintenance_motd);
        } else {
            serverPing.setDescription(event.getResponse().getDescription());
        }
        serverPing.setVersion(event.getResponse().getVersion());
        event.setResponse(serverPing);
    }

    @EventHandler
    public void onPlayerLogin(PostLoginEvent event) {
        if (plugin.isMaintenance()) {
            if (event.getPlayer().hasPermission("mn2.bungee.maintenance") == false) {
                event.getPlayer().disconnect(new TextComponent(plugin.getMainConfig().maintenance_kick));
            }
        }
    }

}
