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
        plugin.getLogger().info("Forced Host: " + server);
        if (server == null) {
            server = getStoredServer(player);
            plugin.getLogger().info("Stored Host: " + server);
            if (server == null) {
                server = getDefault(player);
                plugin.getLogger().info("Default Host: " + server);
            }
        }
        if (server == null) {
            player.disconnect(new TextComponent("Unable to find a server to connect to. Please report"));
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
        ExtendedServerInfo extendedServerInfo = ExtendedServerInfo.getEmptiestServer(forced);
        if (extendedServerInfo == null) {
            return null;
        }
        return extendedServerInfo.getServerInfo();
    }

    private ServerInfo getDefault(ProxiedPlayer player) {
        String defaultServer = player.getPendingConnection().getListener().getDefaultServer();
        ExtendedServerInfo extendedServerInfo = ExtendedServerInfo.getEmptiestServer(defaultServer);
        if (extendedServerInfo == null) {
            return null;
        }
        return extendedServerInfo.getServerInfo();
    }

    private ServerInfo findNameIgnoreSame(ExtendedServerInfo extendedServerInfo) {
        ServerInfo serverInfo = null;
        String serverName = extendedServerInfo.getServerName();
        ArrayList<ServerInfo> serverInfos = new ArrayList<>();
        for (ExtendedServerInfo extendedServerInfo1 : ExtendedServerInfo.getExtendedInfos(serverName)) {
            if (extendedServerInfo1 == extendedServerInfo) {
                continue;
            }
            if (plugin.getProxy().getServers().get(extendedServerInfo1.getServerInfo().getName()) == null) {
                continue;
            }
            if (extendedServerInfo1.getFree() >= 1) {
                serverInfos.add(extendedServerInfo1.getServerInfo());
            }
        }
        int random = (int) (Math.random() * serverInfos.size());
        serverInfo = serverInfos.get(random);
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
        ExtendedServerInfo extendedServerInfo = ExtendedServerInfo.getEmptiestServer(server);
        if (extendedServerInfo == null) {
            return null;
        }
        ServerInfo serverInfo = extendedServerInfo.getServerInfo();
        if (extendedServerInfo instanceof ManualESI) {
            serverInfo = getDefault(proxiedPlayer);
        }
        return serverInfo;
    }

    public void createUser(ProxiedPlayer proxiedPlayer) {
        plugin.getLogger().info("Created user: " + proxiedPlayer.getName());
        DatabaseAPI.getMongoDatabase().insert("mn2_users",
                new BasicDBObject("userUUID", proxiedPlayer.getUniqueId().toString()).append("lastUserName", proxiedPlayer.getName()).append("server", ""));
    }

    @Override
    public void setServer(final ProxiedPlayer proxiedPlayer) {
        ServerInfo serverInfo = proxiedPlayer.getServer().getInfo();
        final ExtendedServerInfo extendedServerInfo = ExtendedServerInfo.getExtendedInfos().get(serverInfo.getName());
        if (extendedServerInfo instanceof ManualESI) {
            return;
        }
        for (String serverName : plugin.getMainConfig().users_serverNames) {
            if (extendedServerInfo.getServerName().startsWith(serverName)) {
                return;
            }
        }
        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
            @Override
            public void run() {
                DatabaseAPI.getMongoDatabase().updateDocument("mn2_users", new BasicDBObject("userUUID", proxiedPlayer.getUniqueId().toString()),
                        new BasicDBObject("$set", new BasicDBObject("server", extendedServerInfo.getServerName())));
            }
        });
    }

    @Override
    public void save() {
    }

    @Override
    public void close() {
    }
}
