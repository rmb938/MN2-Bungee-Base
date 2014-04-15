package com.rmb938.bungee.base;

import com.rmb938.bungee.base.command.*;
import com.rmb938.bungee.base.config.MainConfig;
import com.rmb938.bungee.base.database.DatabaseReconnectHandler;
import com.rmb938.bungee.base.jedis.NetCommandHandlerBTB;
import com.rmb938.bungee.base.jedis.NetCommandHandlerSCTB;
import com.rmb938.bungee.base.jedis.NetCommandHandlerSTB;
import com.rmb938.bungee.base.listeners.PlayerListener;
import com.rmb938.bungee.base.listeners.PluginListener;
import com.rmb938.bungee.base.utils.help.SimpleHelpMap;
import com.rmb938.database.DatabaseAPI;
import com.rmb938.jedis.JedisManager;
import com.rmb938.jedis.net.command.bungee.NetCommandBTSC;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class MN2BungeeBase extends Plugin {

    private MainConfig mainConfig;
    private String IP;
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
            getLogger().log(Level.SEVERE, null ,e);
            return;
        }

        DatabaseAPI.initializeMongo(mainConfig.mongo_database, mainConfig.mongo_address, mainConfig.mongo_port);

        getProxy().setReconnectHandler(new DatabaseReconnectHandler(this));

        getProxy().getServers().clear();

        JedisManager.connectToRedis(mainConfig.redis_address);
        JedisManager.setUpDelegates();

        try {
            Jedis jedis = JedisManager.getJedis();
            for (ListenerInfo listenerInfo : getProxy().getConfig().getListeners()) {
                IP = jedis.get(listenerInfo.getHost().getAddress().getHostAddress()+":bungee");
                jedis.del(listenerInfo.getHost().getAddress().getHostAddress()+":bungee");
            }
            getLogger().info("IP: "+IP);
            JedisManager.returnJedis(jedis);
        } catch (Exception e) {
            getLogger().warning("Unable to connect to redis. Closing");
            getProxy().stop();
            return;
        }

        if (IP == null) {
            getLogger().severe("Error starting server. Unknown internal IP address.");
            getProxy().stop();
        } else {
            getLogger().info("Internal IP: "+IP);
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

        getProxy().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                sendHeartbeat();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        JedisManager.shutDown();
    }

    public SimpleHelpMap getHelpMap() {
        return helpMap;
    }

    public String getIP() {
        return IP;
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    private void sendHeartbeat() {
        NetCommandBTSC netCommandBTSC = new NetCommandBTSC("heartbeat", IP);
        netCommandBTSC.flush();
    }
}
