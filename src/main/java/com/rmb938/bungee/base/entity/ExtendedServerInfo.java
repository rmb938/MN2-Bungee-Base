package com.rmb938.bungee.base.entity;

import net.md_5.bungee.api.config.ServerInfo;

import java.util.HashMap;

public class ExtendedServerInfo {

    private static HashMap<String, ExtendedServerInfo> extendedInfos = new HashMap<>();

    public static HashMap<String, ExtendedServerInfo> getExtendedInfos() {
        return extendedInfos;
    }

    private final ServerInfo serverInfo;
    private final int maxPlayers;
    private int currentPlayers = 0;

    public ExtendedServerInfo(ServerInfo serverInfo, int maxPlayers) {
        this.serverInfo = serverInfo;
        this.maxPlayers = maxPlayers;
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
