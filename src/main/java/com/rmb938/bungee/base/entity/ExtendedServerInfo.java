package com.rmb938.bungee.base.entity;

import net.md_5.bungee.api.config.ServerInfo;

import java.util.ArrayList;
import java.util.HashMap;

public class ExtendedServerInfo {

    private static HashMap<String, ExtendedServerInfo> extendedInfos = new HashMap<>();

    public static HashMap<String, ExtendedServerInfo> getExtendedInfos() {
        return extendedInfos;
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
