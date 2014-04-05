package com.rmb938.bungee.base.database;

import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.bungee.base.entity.ExtendedServerInfo;
import com.rmb938.database.DatabaseAPI;
import net.md_5.bungee.api.AbstractReconnectHandler;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.util.ArrayList;
import java.util.Map;

public class DatabaseReconnectHandler extends AbstractReconnectHandler {

    private final MN2BungeeBase plugin;

    public DatabaseReconnectHandler(MN2BungeeBase plugin) {
        this.plugin = plugin;
        createTable();
    }

    public void createTable() {
        if (DatabaseAPI.getMySQLDatabase().isTable("mn2_users") == false) {
            DatabaseAPI.getMySQLDatabase().createTable("CREATE TABLE IF NOT EXISTS `mn2_users` (" +
                    " `userUUID` varchar(36) NOT NULL," +
                    " `lastUserName` varchar(16) NOT NULL," +
                    " `server` varchar(64) NOT NULL," +
                    " PRIMARY KEY (`userUUID`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1;");
        }
    }

    @Override
    protected ServerInfo getStoredServer(ProxiedPlayer proxiedPlayer) {
        ArrayList<Object> beansInfo = DatabaseAPI.getMySQLDatabase().getBeansInfo("select server from mn2_users where userUUID='" + proxiedPlayer.getUUID() + "'", new MapListHandler());
        if (beansInfo.isEmpty()) {
            createUser(proxiedPlayer);
            return getStoredServer(proxiedPlayer);
        }
        Map map = (Map) beansInfo.get(0);
        String server = (String) map.get("server");
        ArrayList<ServerInfo> serverInfos = new ArrayList<>();
        for (ExtendedServerInfo extendedServerInfo : ExtendedServerInfo.getExtendedInfos().values()) {
            if (extendedServerInfo.getServerName().equalsIgnoreCase(server)) {
                if (extendedServerInfo.getFree() >= 1) {
                    serverInfos.add(extendedServerInfo.getServerInfo());
                }
            }
        }
        if (serverInfos.isEmpty()) {
            return plugin.getProxy().getServerInfo(proxiedPlayer.getPendingConnection().getListener().getDefaultServer());
        }
        int random = (int) (Math.random() * serverInfos.size());
        return serverInfos.get(random);
    }

    public void createUser(ProxiedPlayer proxiedPlayer) {
        plugin.getLogger().info("Created user: " + proxiedPlayer.getName());
        DatabaseAPI.getMySQLDatabase().updateQueryPS("INSERT INTO `mn2_users` (uuid, lastUserName, server) values (?, ?, ?)", proxiedPlayer.getUUID(), proxiedPlayer.getName(), "");
    }

    @Override
    public void setServer(ProxiedPlayer proxiedPlayer) {
        ServerInfo serverInfo = proxiedPlayer.getServer().getInfo();
        for (String serverName : plugin.getMainConfig().serverNames) {
            if (serverInfo.getName().split("\\.")[4].startsWith(serverName)) {
                return;
            }
        }
        DatabaseAPI.getMySQLDatabase().updateQueryPS("UPDATE `mn2_users` SET server = ? where uuid = ?",
                serverInfo.getName().split("\\.")[4], proxiedPlayer.getUUID());
    }

    @Override
    public void save() {
    }

    @Override
    public void close() {
    }
}
