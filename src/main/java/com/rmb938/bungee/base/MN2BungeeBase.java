package com.rmb938.bungee.base;

import com.rmb938.bungee.base.command.*;
import com.rmb938.bungee.base.config.MainConfig;
import com.rmb938.bungee.base.database.DatabaseReconnectHandler;
import com.rmb938.bungee.base.entity.ExtendedServerInfo;
import com.rmb938.bungee.base.jedis.NetCommandHandlerBTB;
import com.rmb938.bungee.base.jedis.NetCommandHandlerSCTB;
import com.rmb938.bungee.base.jedis.NetCommandHandlerSTB;
import com.rmb938.bungee.base.listeners.PlayerListener;
import com.rmb938.bungee.base.listeners.PluginListener;
import com.rmb938.bungee.base.utils.help.SimpleHelpMap;
import com.rmb938.database.DatabaseAPI;
import com.rmb938.jedis.JedisManager;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import org.json.JSONException;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class MN2BungeeBase extends Plugin {

    private MainConfig mainConfig;
    private String privateIP;
    private String publicIP;
    private SimpleHelpMap helpMap;

    @Override
    public void onEnable() {
        helpMap = new SimpleHelpMap(this);
        getLogger().warning("--------------------------------------------------");
        getLogger().warning("Multi-Node Minecraft Network is under the Creative Commons");
        getLogger().warning("Attribution-NonCommercial 4.0 International Public License");
        getLogger().warning("If you are using this in a commercial environment you MUST");
        getLogger().warning("obtain written permission.");
        getLogger().warning("--------------------------------------------------");
        mainConfig = new MainConfig(this);
        try {
            mainConfig.init();
            mainConfig.save();
        } catch (InvalidConfigurationException e) {
            getLogger().log(Level.SEVERE, null, e);
            return;
        }

        getProxy().getServers().clear();

        try {
            for (String manualString : mainConfig.manualServers) {
                String[] split = manualString.split("/");
                String[] connection = split[1].split(":");
                UUID uuid = UUID.randomUUID();
                ServerInfo serverInfo = getProxy().constructServerInfo(uuid.toString(), new InetSocketAddress(connection[0], Integer.parseInt(connection[1])), "", false);
                ExtendedServerInfo extendedServerInfo = new ExtendedServerInfo(serverInfo, 100, split[0], 1);
                ExtendedServerInfo.getExtendedInfos().put(uuid.toString(), extendedServerInfo);
                getProxy().getServers().put(uuid.toString(), serverInfo);
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, null, e);
        }
        try {
            DatabaseAPI.initializeMongo(mainConfig.mongo_database, mainConfig.mongo_address, mainConfig.mongo_port);
        } catch (Exception e) {
            getLogger().warning("Unable to connect to mongo. Closing");
            getProxy().stop();
            return;
        }

        getProxy().setReconnectHandler(new DatabaseReconnectHandler(this));

        JedisManager.connectToRedis(mainConfig.redis_address);
        JedisManager.setUpDelegates();

        try {
            Jedis jedis = JedisManager.getJedis();
            for (ListenerInfo listenerInfo : getProxy().getConfig().getListeners()) {
                String data =  jedis.get(listenerInfo.getHost().getAddress().getHostAddress() + ":bungee");
                JSONObject jsonObject = new JSONObject(data);
                publicIP = listenerInfo.getHost().getAddress().getHostAddress();
                privateIP = jsonObject.getString("privateIP");
            }
            getLogger().info("privateIP: " + privateIP);
            JedisManager.returnJedis(jedis);
        } catch (Exception e) {
            getLogger().warning("Unable to connect to redis. Closing");
            getProxy().stop();
            return;
        }

        if (privateIP == null) {
            getLogger().severe("Error starting server. Unknown private IP address.");
            getProxy().stop();
        } else {
            getLogger().info("Private IP: " + privateIP);
        }


        new NetCommandHandlerSCTB(this);
        new NetCommandHandlerSTB(this);
        new NetCommandHandlerBTB(this);

        new PlayerListener(this);
        new PluginListener(this);

        ExtendedCommand.registerCommand(this, new CommandHelp(this));
        ExtendedCommand.registerCommand(this, new CommandServer(this));
        ExtendedCommand.registerCommand(this, new CommandList(this));
        ExtendedCommand.registerCommand(this, new CommandStopNode(this));
        ExtendedCommand.registerCommand(this, new CommandStopNetwork(this));
        ExtendedCommand.registerCommand(this, new CommandStopType(this));
        ExtendedCommand.registerCommand(this, new CommandRefreshServerInfo(this));
        final Plugin plugin = this;
        getProxy().getScheduler().schedule(this, new Runnable() {

            @Override
            public void run() {
                sendHeartbeat();
                ArrayList<ExtendedServerInfo> toRemove = new ArrayList<>();
                Jedis jedis = JedisManager.getJedis();
                for (ExtendedServerInfo serverInfo : ExtendedServerInfo.getExtendedInfos().values()) {
                    String data = jedis.get("server." + serverInfo.getServerName() + "." + serverInfo.getServerId());
                    if (data == null) {
                        toRemove.add(serverInfo);
                        continue;
                    }
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        int currentPlayers = jsonObject.getInt("currentPlayers");
                        serverInfo.setCurrentPlayers(currentPlayers);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                JedisManager.returnJedis(jedis);
                for (ExtendedServerInfo serverInfo : toRemove) {
                    final String uuid = serverInfo.getServerInfo().getName();
                    getProxy().getScheduler().schedule(plugin, new Runnable() {
                        @Override
                        public void run() {
                            ExtendedServerInfo.getExtendedInfos().remove(uuid);
                        }
                    }, 10L, TimeUnit.SECONDS);
                    getProxy().getServers().remove(uuid);
                }
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        Jedis jedis = JedisManager.getJedis();
        jedis.del(publicIP + ":bungee");
        JedisManager.returnJedis(jedis);
        JedisManager.shutDown();
    }

    public static ServerInfo getRandomServer(String serverType) {
        ArrayList<ServerInfo> serverInfos = new ArrayList<>();
        for (ExtendedServerInfo extendedServerInfo : ExtendedServerInfo.getExtendedInfos(serverType)) {
            if (extendedServerInfo.getFree() > 1) {
                serverInfos.add(extendedServerInfo.getServerInfo());
            }
        }
        if (serverInfos.isEmpty() == false) {
            int random = (int) (Math.random() * serverInfos.size());
            return serverInfos.get(random);
        }
        return null;
    }

    public String getPublicIP() {
        return publicIP;
    }

    public SimpleHelpMap getHelpMap() {
        return helpMap;
    }

    public String getPrivateIP() {
        return privateIP;
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    private void sendHeartbeat() {
        Jedis jedis = JedisManager.getJedis();
        String data =  jedis.get(publicIP + ":bungee");
        try {
            JSONObject jsonObject = new JSONObject(data);
            jsonObject.put("currentPlayers", getProxy().getPlayers().size());
            jedis.set(publicIP+":bungee", jsonObject.toString());
            jedis.expire(publicIP+":bungee", 60);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JedisManager.returnJedis(jedis);
    }
}
