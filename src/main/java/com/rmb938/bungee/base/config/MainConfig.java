package com.rmb938.bungee.base.config;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Comments;
import net.cubespace.Yamler.Config.Config;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;

public class MainConfig extends Config {

    public MainConfig(Plugin plugin) {
        CONFIG_HEADER = new String[]{"MN2 Bungee Base Configuration File"};
        CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");
    }

    @Comments({"If we should try and reconnect to the default server first or a similar server first.",
                "If we can't connect to a similar server we try to connect to the last server the user was on.",
                "If we can't connect to the last server the user was on then we connect to the default server."})
    public boolean users_reconnectDefault = false;
    @Comment("If the kick message of a server contains any of these words don't try to reconnect the user.")
    public ArrayList<String> users_kickBlacklist = new ArrayList<String>() {{
        add("kick");
        add("ban");
        add("whitelist");
        add("maintenance");
    }};
    @Comment("List of server name prefixes to not reconnect to")
    public ArrayList<String> users_serverNames = new ArrayList<String>() {{
        add("mg");
    }};

    @Comment("The IP address for the redis server")
    public String redis_address = "127.0.0.1";

    @Comment("The IP address from the mongo server")
    public String mongo_address = "127.0.0.1";
    @Comment("The port for the mongo server")
    public int mongo_port = 27017;
    @Comment("The database name for the mongo server")
    public String mongo_database = "minecraft";

    @Comment("A list of server names, IP addresses and ports to ALWAYS add to the bungee server list")
    public ArrayList<String> manualServers = new ArrayList<String>() {{
        add("manual1/192.168.1.1:96487");
        add("manual2/192.168.1.1:98745");
    }};


}
