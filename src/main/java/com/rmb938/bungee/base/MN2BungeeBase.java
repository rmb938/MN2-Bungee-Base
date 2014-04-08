package com.rmb938.bungee.base;

import com.rmb938.bungee.base.command.CommandList;
import com.rmb938.bungee.base.command.CommandMaintenance;
import com.rmb938.bungee.base.command.CommandServer;
import com.rmb938.bungee.base.config.MainConfig;
import com.rmb938.bungee.base.database.DatabaseReconnectHandler;
import com.rmb938.bungee.base.jedis.NetCommandHandlerBTB;
import com.rmb938.bungee.base.jedis.NetCommandHandlerSCTB;
import com.rmb938.bungee.base.jedis.NetCommandHandlerSTB;
import com.rmb938.bungee.base.listeners.PlayerListener;
import com.rmb938.bungee.base.listeners.PluginListener;
import com.rmb938.database.DatabaseAPI;
import com.rmb938.jedis.JedisManager;
import com.rmb938.jedis.net.command.bungee.NetCommandBTSC;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class MN2BungeeBase extends Plugin {

    private MainConfig mainConfig;
    private boolean maintenance;
    private String IP;

    @Override
    public void onEnable() {
        maintenance = true;
        mainConfig = new MainConfig(this);
        try {
            mainConfig.init();
            mainConfig.save();
        } catch (InvalidConfigurationException e) {
            getLogger().log(Level.SEVERE, null ,e);
            return;
        }

        for (ListenerInfo listenerInfo : getProxy().getConfig().getListeners()) {
            if (listenerInfo.getMaxPlayers() == 0) {
                IP = listenerInfo.getHost().getAddress().getHostAddress();
                break;
            }
        }

        if (IP == null) {
            getLogger().severe("Error starting server. Unknown internal IP address.");
            getProxy().stop();
        } else {
            getLogger().info("Internal IP: "+IP);
        }

        DatabaseAPI.initializeMySQL(mainConfig.mySQL_userName, mainConfig.mySQL_password, mainConfig.mySQL_database, mainConfig.mySQL_address, mainConfig.mySQL_port);

        getProxy().setReconnectHandler(new DatabaseReconnectHandler(this));

        getProxy().getServers().clear();

        JedisManager.connectToRedis(mainConfig.redis_address);
        JedisManager.setUpDelegates();

        try {
            JedisManager.returnJedis(JedisManager.getJedis());
        } catch (Exception e) {
            getLogger().warning("Unable to connect to redis. Closing");
            getProxy().stop();
            return;
        }

        new NetCommandHandlerBTB(this);
        new NetCommandHandlerSCTB(this);
        new NetCommandHandlerSTB(this);

        new PlayerListener(this);
        new PluginListener(this);

        getProxy().getPluginManager().registerCommand(this, new CommandMaintenance(this));
        getProxy().getPluginManager().registerCommand(this, new CommandServer(this));
        getProxy().getPluginManager().registerCommand(this, new CommandList(this));

        getProxy().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                maintenance = false;
                getLogger().info("Removing Maintenance Mode");
            }
        }, 30L, TimeUnit.SECONDS);

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

    public String getIP() {
        return IP;
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    private void sendHeartbeat() {
        NetCommandBTSC netCommandBTSC = new NetCommandBTSC("heartbeat", IP);
        netCommandBTSC.flush();
    }
}
