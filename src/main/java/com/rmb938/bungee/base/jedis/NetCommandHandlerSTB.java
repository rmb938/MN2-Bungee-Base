package com.rmb938.bungee.base.jedis;

import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.bungee.base.entity.ExtendedServerInfo;
import com.rmb938.jedis.JedisManager;
import com.rmb938.jedis.net.NetChannel;
import com.rmb938.jedis.net.NetCommandHandler;
import com.rmb938.jedis.net.command.bungee.NetCommandBTS;
import net.md_5.bungee.api.config.ServerInfo;
import org.json.JSONException;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
            final String fromServer = jsonObject.getString("from");

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

                        Jedis jedis = JedisManager.getJedis();
                        while (jedis.setnx("lock." + serverName+".key", System.currentTimeMillis() + 30000 + "") == 0) {
                            String lock = jedis.get("lock." + serverName+".key");
                            long time = Long.parseLong(lock != null ? lock : "0");
                            if (System.currentTimeMillis() > time) {
                                time = Long.parseLong(jedis.getSet("lock." + serverName+".key", System.currentTimeMillis() + 30000 + ""));
                                if (System.currentTimeMillis() < time) {
                                    continue;
                                }
                            } else {
                                continue;
                            }
                            break;
                        }
                        Set<String> keys = jedis.keys("server." +serverName + ".*");
                        int id = -1;
                        for (String key : keys) {
                            String uuid = jedis.get(key);
                            if (uuid.equals(serverInfo.getName())) {
                                id = Integer.parseInt(key.split("\\.")[2]);
                                break;
                            }
                        }
                        jedis.del("lock." + serverName+".key");
                        JedisManager.returnJedis(jedis);

                        if (id == -1) {
                            plugin.getLogger().severe("Server ID -1 for server "+IP+" "+port);
                            NetCommandBTS netCommandBTS = new NetCommandBTS("shutdown", plugin.getIP(), fromServer);
                            netCommandBTS.flush();
                            break;
                        }

                        plugin.getProxy().getServers().put(fromServer, serverInfo);
                        extendedServerInfo = new ExtendedServerInfo(serverInfo, maxPlayers, serverName);
                        ExtendedServerInfo.getExtendedInfos().put(fromServer, extendedServerInfo);
                    }
                    extendedServerInfo.setCurrentPlayers(currentPlayers);
                    if (extendedServerInfo.getServerId() == -1) {
                        plugin.getLogger().severe("Server ID -1 for server "+IP+" "+port);
                        NetCommandBTS netCommandBTS = new NetCommandBTS("shutdown", plugin.getIP(), fromServer);
                        netCommandBTS.flush();
                    }
                    break;
                case "removeServer":
                    plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
                        @Override
                        public void run() {
                            ExtendedServerInfo.getExtendedInfos().remove(fromServer);
                        }
                    }, 10L, TimeUnit.SECONDS);
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
