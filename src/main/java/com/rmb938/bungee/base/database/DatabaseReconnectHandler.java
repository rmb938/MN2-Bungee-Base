package com.rmb938.bungee.base.database;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.bungee.base.entity.ExtendedServerInfo;
import com.rmb938.bungee.base.entity.ManualESI;
import com.rmb938.bungee.base.event.GetStoredEvent;
import com.rmb938.database.DatabaseAPI;
import net.md_5.bungee.api.AbstractReconnectHandler;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;

public class DatabaseReconnectHandler extends AbstractReconnectHandler {

    private final MN2BungeeBase plugin;

    public DatabaseReconnectHandler(MN2BungeeBase plugin) {
        this.plugin = plugin;
        createTable();
    }

    public void createTable() {
        if (DatabaseAPI.getMongoDatabase().collectionExists("mn2_users") == false) {
            DatabaseAPI.getMongoDatabase().createCollection("mn2_users");
        }
    }

    @Override
    public ServerInfo getServer(ProxiedPlayer player) {
        ServerInfo server = DatabaseReconnectHandler.getForcedHost(player.getPendingConnection());
        if (server == null) {
            server = getStoredServer(player);
            if (server == null) {
                server = getDefault(player);
            }
        }
        if (server == null) {
            player.disconnect(new TextComponent("Unable to find a server to connect to. Please report"));
            return null;
        }
        return server;
    }


    public static ServerInfo getForcedHost(PendingConnection con) {
        if (con.getVirtualHost() == null) {
            return null;
        }

        String forced = con.getListener().getForcedHosts().get(con.getVirtualHost().getHostString());

        if (forced == null && con.getListener().isForceDefault()) {
            forced = con.getListener().getDefaultServer();
        }
        ServerInfo serverInfo = null;
        ArrayList<ServerInfo> serverInfos = new ArrayList<>();
        for (ExtendedServerInfo extendedServerInfo : ExtendedServerInfo.getExtendedInfos().values()) {
            if (extendedServerInfo.getServerName().equalsIgnoreCase(forced)) {
                if (extendedServerInfo.getFree() >= 1) {
                    serverInfos.add(extendedServerInfo.getServerInfo());
                }
            }
        }
        if (serverInfos.isEmpty() == false) {
            int random = (int) (Math.random() * serverInfos.size());
            serverInfo = serverInfos.get(random);
        }
        return serverInfo;
    }

    private ServerInfo getDefault(ProxiedPlayer player) {
        ServerInfo server = null;
        String defaultServer = player.getPendingConnection().getListener().getDefaultServer();
        ArrayList<ServerInfo> serverInfos = new ArrayList<>();
        for (ExtendedServerInfo extendedServerInfo : ExtendedServerInfo.getExtendedInfos().values()) {
            if (extendedServerInfo.getServerName().equalsIgnoreCase(defaultServer)) {
                if (plugin.getProxy().getServers().get(extendedServerInfo.getServerInfo().getName()) == null) {
                    continue;
                }
                if (player.getServer() != null) {
                    ExtendedServerInfo extendedServerInfo1 = ExtendedServerInfo.getExtendedInfos().get(player.getServer().getInfo().getName());
                    if (extendedServerInfo == extendedServerInfo1) {
                        continue;
                    }
                }
                if (extendedServerInfo.getFree() >= 1) {
                    serverInfos.add(extendedServerInfo.getServerInfo());
                }
            }
        }
        if (serverInfos.isEmpty() == false) {
            int random = (int) (Math.random() * serverInfos.size());
            server = serverInfos.get(random);
        }
        return server;
    }

    private ServerInfo findNameIgnoreSame(ExtendedServerInfo extendedServerInfo) {
        ServerInfo serverInfo = null;
        String serverName = extendedServerInfo.getServerName();
        ArrayList<ServerInfo> serverInfos = new ArrayList<>();
        for (ExtendedServerInfo extendedServerInfo1 : ExtendedServerInfo.getExtendedInfos().values()) {
            if (extendedServerInfo1 == extendedServerInfo) {
                continue;
            }
            if (plugin.getProxy().getServers().get(extendedServerInfo1.getServerInfo().getName()) == null) {
                continue;
            }
            if (extendedServerInfo1.getServerName().equalsIgnoreCase(serverName)) {
                if (extendedServerInfo1.getFree() >= 1) {
                    serverInfos.add(extendedServerInfo1.getServerInfo());
                }
            }
        }
        if (serverInfos.isEmpty() == false) {
            int random = (int) (Math.random() * serverInfos.size());
            serverInfo = serverInfos.get(random);
        }
        /*if (serverInfo != null) {
            plugin.getLogger().info("Server Info: " + serverInfo.getName());
        }*/
        return serverInfo;
    }

    public ServerInfo getSimilarServer(ProxiedPlayer player, ServerInfo serverInfo) {
        ServerInfo serverInfo1 = null;
        ExtendedServerInfo extendedServerInfo = ExtendedServerInfo.getExtendedInfos().get(serverInfo.getName());
        if (extendedServerInfo == null) {
            if (plugin.getMainConfig().users_reconnectDefault == false) {
                serverInfo1 = getStoredServer(player);
            } else {
                serverInfo1 = getDefault(player);
            }
            //plugin.getLogger().info("Extended = null");
        } else {
            if (plugin.getMainConfig().users_reconnectDefault == false) {
                boolean reconnect = true;
                for (String serverName : plugin.getMainConfig().users_serverNames) {
                    if (extendedServerInfo.getServerName().startsWith(serverName)) {
                        reconnect = false;
                        break;
                    }
                }
                if (reconnect == true) {
                    serverInfo1 = findNameIgnoreSame(extendedServerInfo);
                }
                if (serverInfo1 == null) {
                    serverInfo1 = getStoredServer(player);
                    ExtendedServerInfo extendedServerInfo1 = ExtendedServerInfo.getExtendedInfos().get(serverInfo1.getName());
                    if (extendedServerInfo == extendedServerInfo1) {
                        serverInfo1 = findNameIgnoreSame(extendedServerInfo);
                    }
                }
            } else {
                serverInfo1 = getDefault(player);
                if (serverInfo1 != null) {
                    ExtendedServerInfo extendedServerInfo1 = ExtendedServerInfo.getExtendedInfos().get(serverInfo1.getName());
                    if (extendedServerInfo == extendedServerInfo1) {
                        serverInfo1 = findNameIgnoreSame(extendedServerInfo);
                    }
                }
            }
        }
        return serverInfo1;
    }

    @Override
    protected ServerInfo getStoredServer(ProxiedPlayer proxiedPlayer) {
        DBObject userObject = DatabaseAPI.getMongoDatabase().findOne("mn2_users", new BasicDBObject("userUUID", proxiedPlayer.getUniqueId().toString()));
        if (userObject == null) {
            plugin.getLogger().info("No user found for " + proxiedPlayer.getName() + " (" + proxiedPlayer.getUniqueId().toString() + ") creating new user.");
            createUser(proxiedPlayer);
            return getStoredServer(proxiedPlayer);
        }
        GetStoredEvent event = new GetStoredEvent(proxiedPlayer);
        plugin.getProxy().getPluginManager().callEvent(event);
        String server = (String) userObject.get("server");
        ArrayList<ServerInfo> serverInfos = new ArrayList<>();
        for (ExtendedServerInfo extendedServerInfo : ExtendedServerInfo.getExtendedInfos().values()) {
            if (extendedServerInfo instanceof ManualESI) {
                continue;
            }
            if (plugin.getProxy().getServers().get(extendedServerInfo.getServerInfo().getName()) == null) {
                continue;
            }
            if (extendedServerInfo.getServerName().equalsIgnoreCase(server)) {
                if (extendedServerInfo.getFree() >= 1) {
                    serverInfos.add(extendedServerInfo.getServerInfo());
                }
            }
        }
        if (serverInfos.isEmpty()) {
            return null;
        }
        int random = (int) (Math.random() * serverInfos.size());
        ServerInfo serverInfo = serverInfos.get(random);
        //plugin.getLogger().info("Server Name: "+serverInfo.getName());
        return serverInfo;
    }

    public void createUser(ProxiedPlayer proxiedPlayer) {
        plugin.getLogger().info("Created user: " + proxiedPlayer.getName());
        DatabaseAPI.getMongoDatabase().insert("mn2_users",
                new BasicDBObject("userUUID", proxiedPlayer.getUniqueId().toString()).append("lastUserName", proxiedPlayer.getName()).append("server", ""));
    }

    @Override
    public void setServer(ProxiedPlayer proxiedPlayer) {
        ServerInfo serverInfo = proxiedPlayer.getServer().getInfo();
        ExtendedServerInfo extendedServerInfo = ExtendedServerInfo.getExtendedInfos().get(serverInfo.getName());
        if (extendedServerInfo instanceof ManualESI) {
            return;
        }
        for (String serverName : plugin.getMainConfig().users_serverNames) {
            if (extendedServerInfo.getServerName().startsWith(serverName)) {
                return;
            }
        }
        DatabaseAPI.getMongoDatabase().updateDocument("mn2_users", new BasicDBObject("userUUID", proxiedPlayer.getUniqueId().toString()),
                new BasicDBObject("$set", new BasicDBObject("server", extendedServerInfo.getServerName())));
    }

    @Override
    public void save() {
    }

    @Override
    public void close() {
    }
}
