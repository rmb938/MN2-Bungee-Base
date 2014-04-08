package com.rmb938.bungee.base.entity;

import com.rmb938.jedis.JedisManager;
import net.md_5.bungee.api.config.ServerInfo;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ExtendedServerInfo {

    private final static ConcurrentHashMap<String, ExtendedServerInfo> extendedInfos = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, ExtendedServerInfo> getExtendedInfos() {
        synchronized (extendedInfos) {
            return extendedInfos;
        }
    }

    public static ArrayList<ExtendedServerInfo> getExtendedInfos(String serverName) {
        ArrayList<ExtendedServerInfo> exSI = new ArrayList<>();
        for (ExtendedServerInfo extendedServerInfo : extendedInfos.values()) {
            if (extendedServerInfo.getServerName().equalsIgnoreCase(serverName)) {
                exSI.add(extendedServerInfo);
            }
        }
        return exSI;
    }

    private final ServerInfo serverInfo;
    private final int maxPlayers;
    private final String serverName;
    private int currentPlayers = 0;

    public ExtendedServerInfo(ServerInfo serverInfo, int maxPlayers, String serverName) {
        this.serverInfo = serverInfo;
        this.maxPlayers = maxPlayers;
        this.serverName = serverName;
    }

    public int getServerId() {
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
        return id;
    }

    public String getServerName() {
        return serverName;
    }

    public int getFree() {
        return getMaxPlayers() - getCurrentPlayers();
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getCurrentPlayers() {
        return currentPlayers;
    }

    public void setCurrentPlayers(int currentPlayers) {
        this.currentPlayers = currentPlayers;
    }


}
