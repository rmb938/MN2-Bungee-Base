package com.rmb938.bungee.base.jedis;

import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.bungee.base.database.DatabaseReconnectHandler;
import com.rmb938.bungee.base.entity.ExtendedServerInfo;
import com.rmb938.jedis.net.NetChannel;
import com.rmb938.jedis.net.NetCommandHandler;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.logging.Level;

public class NetCommandHandlerSTB extends NetCommandHandler {

    private final MN2BungeeBase plugin;

    public NetCommandHandlerSTB(MN2BungeeBase plugin) {
        NetCommandHandler.addHandler(NetChannel.SERVER_TO_BUNGEE, this);
        this.plugin = plugin;
    }

    @Override
    public void handle(JSONObject jsonObject) {
        try {
            String fromServer = jsonObject.getString("from");

            String command = jsonObject.getString("command");
            HashMap<String, Object> objectHashMap = objectToHashMap(jsonObject.getJSONObject("data"));
            switch (command) {
                case "updateServer":
                    String IP = (String) objectHashMap.get("IP");
                    int port = (Integer) objectHashMap.get("port");
                    String serverName = (String) objectHashMap.get("serverName");
                    int currentPlayers = (Integer) objectHashMap.get("currentPlayers");
                    int maxPlayers = (Integer) objectHashMap.get("maxPlayers");
                    ExtendedServerInfo extendedServerInfo = ExtendedServerInfo.getExtendedInfos().get(fromServer);

                    if (extendedServerInfo == null) {
                        ServerInfo serverInfo = plugin.getProxy().constructServerInfo(fromServer, new InetSocketAddress(IP, port), "", false);
                        plugin.getProxy().getServers().put(fromServer, serverInfo);
                        extendedServerInfo = new ExtendedServerInfo(serverInfo, maxPlayers, serverName);
                        ExtendedServerInfo.getExtendedInfos().put(fromServer, extendedServerInfo);
                    }
                    plugin.getLogger().info("Heartbeat from "+extendedServerInfo.getServerName());
                    extendedServerInfo.setCurrentPlayers(currentPlayers);
                    break;
                case "removeServer":
                    ExtendedServerInfo.getExtendedInfos().remove(fromServer);
                    ServerInfo serverInfo = plugin.getProxy().getServerInfo(fromServer);
                    for (ProxiedPlayer proxiedPlayer : serverInfo.getPlayers()) {
                        ServerInfo newServer = ((DatabaseReconnectHandler)plugin.getProxy().getReconnectHandler()).getSimilarServer(proxiedPlayer, serverInfo);
                        if (newServer != null) {
                            proxiedPlayer.sendMessage(new TextComponent("The server you were on unexpectedly disconnected."));
                            proxiedPlayer.connect(newServer);
                        } else {
                            proxiedPlayer.disconnect(new TextComponent("The server you were on unexpectedly disconnected."));
                        }
                    }
                    plugin.getProxy().getServers().remove(fromServer);
                    break;
                default:
                    plugin.getLogger().info("Unknown STB Command MN2BukkitBase " + command);
            }
        } catch (JSONException e) {
            plugin.getLogger().log(Level.SEVERE, null, e);
        }
    }
}
