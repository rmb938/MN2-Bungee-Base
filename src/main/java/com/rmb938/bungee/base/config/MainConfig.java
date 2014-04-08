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

    @Comments({"Save users to the database",
                "If set to false users will always initially connect to the default server"})
    public boolean users_save = true;
    @Comments({"If we should try and reconnect to the default server first or a similar server first.",
                "If we can't connect to a similar server we try to connect to the last server the user was on.",
                "If we can't connect to the last server the user was on then we connect to the default server."})
    public boolean users_reconnectDefault = false;
    @Comment("If the kick message of a server contains any of these words don't try to reconnect the user.")
    public ArrayList<String> users_kickBlacklist = new ArrayList<String>() {{
        add("kick");
        add("ban");
    }};
    @Comment("List of server name prefixes to not reconnect to")
    public ArrayList<String> users_serverNames = new ArrayList<String>() {{
        add("mg");
    }};

    @Comment("MOTD to show while in maintenance mode")
    public String maintenance_motd = "Maintenance Mode";
    @Comment("Kick message to show while in maintenance mode")
    public String maintenance_kick = "Server is in Maintenance Mode";

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



}
