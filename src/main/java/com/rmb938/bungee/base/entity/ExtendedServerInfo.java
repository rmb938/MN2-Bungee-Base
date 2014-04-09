package com.rmb938.bungee.base.entity;

import net.md_5.bungee.api.config.ServerInfo;

import java.util.ArrayList;
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
    private int serverId;

    public ExtendedServerInfo(ServerInfo serverInfo, int maxPlayers, String serverName, int serverId) {
        this.serverInfo = serverInfo;
        this.maxPlayers = maxPlayers;
        this.serverName = serverName;
        this.serverId = serverId;
    }

    public int getServerId() {
        return serverId;
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
