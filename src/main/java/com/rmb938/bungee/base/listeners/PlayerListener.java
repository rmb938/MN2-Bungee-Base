package com.rmb938.bungee.base.listeners;

import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.bungee.base.database.DatabaseReconnectHandler;
import com.rmb938.bungee.base.entity.ExtendedServerInfo;
import com.rmb938.bungee.base.entity.ManualESI;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
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
            if (extendedServerInfo instanceof ManualESI) {
                continue;
            }
            max += extendedServerInfo.getMaxPlayers();
            online += extendedServerInfo.getCurrentPlayers();
        }
        ServerPing serverPing = new ServerPing();
        ServerPing.Players players = new ServerPing.Players(max, online, event.getResponse().getPlayers().getSample());
        serverPing.setPlayers(players);
        serverPing.setDescription(event.getResponse().getDescription());
        serverPing.setVersion(event.getResponse().getVersion());
        serverPing.setFavicon(event.getResponse().getFavicon());
        event.setResponse(serverPing);
    }

    @EventHandler
    public void onServerKick(ServerKickEvent event) {
        for (String word : plugin.getMainConfig().users_kickBlacklist) {
            if (event.getKickReason().toLowerCase().contains(word.toLowerCase())) {
                return;
            }
        }
        ServerInfo newServer = ((DatabaseReconnectHandler)plugin.getProxy().getReconnectHandler()).getSimilarServer(event.getPlayer(), event.getPlayer().getServer().getInfo());
        //ServerInfo newServer = ((DatabaseReconnectHandler)plugin.getProxy().getReconnectHandler()).getServer(event.getPlayer());
        if (newServer != null) {
            event.getPlayer().sendMessage(new TextComponent("The server you were on unexpectedly disconnected."));
        } else {
            event.setKickReason("The server you were on unexpectedly disconnected.");
            return;
        }
        event.setCancelled(true);
        event.setCancelServer(newServer);
    }

}
