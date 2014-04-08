package com.rmb938.bungee.base.entity;

import net.md_5.bungee.api.config.ServerInfo;

import java.util.concurrent.ConcurrentHashMap;

public class ExtendedServerInfo {

    private final static ConcurrentHashMap<String, ExtendedServerInfo> extendedInfos = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, ExtendedServerInfo> getExtendedInfos() {
        synchronized (extendedInfos) {
            return extendedInfos;
        }
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
