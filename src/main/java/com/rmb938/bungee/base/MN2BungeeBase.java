package com.rmb938.bungee.base;

import com.rmb938.bungee.base.command.CommandMaintenance;
import com.rmb938.bungee.base.config.MainConfig;
import com.rmb938.bungee.base.database.DatabaseReconnectHandler;
import com.rmb938.bungee.base.jedis.NetCommandHandlerBTB;
import com.rmb938.bungee.base.jedis.NetCommandHandlerSCTB;
import com.rmb938.bungee.base.jedis.NetCommandHandlerSTB;
import com.rmb938.bungee.base.listeners.PlayerListener;
import com.rmb938.database.DatabaseAPI;
import com.rmb938.jedis.JedisManager;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class MN2BungeeBase extends Plugin {

    private MainConfig mainConfig;
    private boolean maintenance;

    @Override
    public void onEnable() {
        maintenance = true;
        mainConfig = new MainConfig(this);
        try {
            mainConfig.init();
        } catch (InvalidConfigurationException e) {
            getLogger().log(Level.SEVERE, null ,e);
            return;
        }

        DatabaseAPI.initializeMySQL(mainConfig.mySQL_userName, mainConfig.mySQL_password, mainConfig.mySQL_database, mainConfig.mySQL_address, mainConfig.mySQL_port);

        getProxy().setReconnectHandler(new DatabaseReconnectHandler(this));

        getProxy().getServers().clear();

        JedisManager.connectToRedis(mainConfig.redis_address);
        JedisManager.setUpDelegates();

        new NetCommandHandlerBTB(this);
        new NetCommandHandlerSCTB(this);
        new NetCommandHandlerSTB(this);

        new PlayerListener(this);

        getProxy().getPluginManager().registerCommand(this, new CommandMaintenance(this));

        getProxy().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                maintenance = false;
            }
        }, 30L, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        JedisManager.shutDown();
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
}
