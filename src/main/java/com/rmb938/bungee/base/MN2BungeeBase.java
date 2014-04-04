package com.rmb938.bungee.base;

import com.rmb938.jedis.JedisManager;
import net.md_5.bungee.api.plugin.Plugin;

public class MN2BungeeBase extends Plugin {

    @Override
    public void onEnable() {
        JedisManager.connectToRedis("");
        JedisManager.setUpDelegates();
    }

    @Override
    public void onDisable() {
        JedisManager.shutDown();
    }

}
