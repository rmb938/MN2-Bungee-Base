package com.rmb938.bungee.base.database;

import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.bungee.base.entity.ExtendedServerInfo;
import com.rmb938.database.DatabaseAPI;
import net.md_5.bungee.api.AbstractReconnectHandler;
import net.md_5.bungee.api.chat.TextComponent;
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
                    " `userUUID` varchar(37) NOT NULL," +
                    " `lastUserName` varchar(16) NOT NULL," +
                    " `server` varchar(64) NOT NULL," +
                    " PRIMARY KEY (`userUUID`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1;");
        }
    }

    @Override
    public ServerInfo getServer(ProxiedPlayer player) {
        ServerInfo server = getForcedHost(player.getPendingConnection());
        if (server == null) {
            server = getStoredServer(player);
            if (server == null) {
                plugin.getLogger().info("Stored = null");
                String defaultServer = player.getPendingConnection().getListener().getDefaultServer();
                plugin.getLogger().info("Default Server: "+defaultServer);
                for (ExtendedServerInfo extendedServerInfo : ExtendedServerInfo.getExtendedInfos().values()) {
                    plugin.getLogger().info("Tested Server: "+extendedServerInfo.getServerName());
                    if (extendedServerInfo.getServerName().equalsIgnoreCase(defaultServer)) {
                        if (extendedServerInfo.getFree() >= 1) {
                            server = extendedServerInfo.getServerInfo();
                            plugin.getLogger().info("Found server");
                            break;
                        }
                    }
                }
            }
        }
        if (server == null) {
            player.disconnect(new TextComponent("Unable to find a server to connect to. Please report"));
        }
        return server;
    }

    @Override
    protected ServerInfo getStoredServer(ProxiedPlayer proxiedPlayer) {
        ArrayList<Object> beansInfo = DatabaseAPI.getMySQLDatabase().getBeansInfo("select server from mn2_users where userUUID='" + proxiedPlayer.getUniqueId().toString() + "'", new MapListHandler());
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
        DatabaseAPI.getMySQLDatabase().updateQueryPS("INSERT INTO `mn2_users` (userUUID, lastUserName, server) values (?, ?, ?)", proxiedPlayer.getUniqueId().toString(), proxiedPlayer.getName(), "");
    }

    @Override
    public void setServer(ProxiedPlayer proxiedPlayer) {
        ServerInfo serverInfo = proxiedPlayer.getServer().getInfo();
        ExtendedServerInfo extendedServerInfo = ExtendedServerInfo.getExtendedInfos().get(serverInfo.getName());
        for (String serverName : plugin.getMainConfig().serverNames) {
            if (extendedServerInfo.getServerName().startsWith(serverName)) {
                return;
            }
        }
        DatabaseAPI.getMySQLDatabase().updateQueryPS("UPDATE `mn2_users` SET server = ? where userUUID = ?",
                extendedServerInfo.getServerName(), proxiedPlayer.getUniqueId().toString());
    }

    @Override
    public void save() {
    }

    @Override
    public void close() {
    }
}
