package com.rmb938.bungee.base.command;

import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.bungee.base.entity.ExtendedServerInfo;
import com.rmb938.jedis.net.command.bungee.NetCommandBTB;
import com.rmb938.jedis.net.command.bungee.NetCommandBTS;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.concurrent.TimeUnit;

public class CommandStopType extends ExtendedCommand {

    private final MN2BungeeBase plugin;

    public CommandStopType(MN2BungeeBase plugin) {
        super(plugin, "stoptype", "mn2.bungee.stop.type");
        this.setUsage("/<command> [serverType]");
        this.setDescription("Stops a specific type of server on the minecraft network");
        this.plugin = plugin;
    }

    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(new TextComponent("Usage: /stoptype [serverType]"));
            return;
        }
        final String serverType = args[0];
        if (serverType.equalsIgnoreCase("bungee")) {
            plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
                @Override
                public void run() {
                    NetCommandBTB netCommandBTB = new NetCommandBTB("stop", plugin.getIP(), "*");
                    netCommandBTB.flush();
                }
            }, 10, TimeUnit.SECONDS);
            sender.sendMessage(new TextComponent("Stopping Bungee Servers..."));
            return;
        }
        plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
            @Override
            public void run() {
                for (ExtendedServerInfo extendedServerInfo : ExtendedServerInfo.getExtendedInfos().values()) {
                    if (extendedServerInfo.getServerName().equalsIgnoreCase(serverType)) {
                        NetCommandBTS netCommandBTS = new NetCommandBTS("shutdown", plugin.getIP(), extendedServerInfo.getServerInfo().getName());
                        netCommandBTS.flush();
                    }
                }
            }
        }, 10, TimeUnit.SECONDS);
        sender.sendMessage(new TextComponent("Stopping "+serverType+" Servers..."));

    }
}
