package com.rmb938.bungee.base.config;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Config;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;

public class MainConfig extends Config {

    public MainConfig(Plugin plugin) {
        CONFIG_HEADER = new String[]{"MN2 Bungee Base Configuration File"};
        CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");
    }

    @Comment("The IP address for the redis server")
    public String redis_address = "127.0.0.1";

    @Comment("The IP address from the mySQL server")
    public String mySQL_address = "127.0.0.1";
    @Comment("The port for the mySQL server")
    public int mySQL_port = 3306;
    @Comment("The username for the mySQL server")
    public String mySQL_userName = "userName";
    @Comment("The password for the mySQL server")
    public String mySQL_password = "password";
    @Comment("The database name for the mySQL server")
    public String mySQL_database = "database";

    @Comment("List of server name prefixes to not reconnect to")
    public ArrayList<String> serverNames = new ArrayList<String>() {{
        add("mg");
    }};


}
