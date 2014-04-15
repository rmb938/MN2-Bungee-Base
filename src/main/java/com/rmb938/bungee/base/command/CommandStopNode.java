package com.rmb938.bungee.base.command;

import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.bungee.base.entity.ExtendedServerInfo;
import com.rmb938.jedis.net.command.bungee.NetCommandBTS;
import com.rmb938.jedis.net.command.bungee.NetCommandBTSC;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.concurrent.TimeUnit;

public class CommandStopNode extends ExtendedCommand {

    private final MN2BungeeBase plugin;

    public CommandStopNode(MN2BungeeBase plugin) {
        super(plugin, "stopnode", "mn2.bungee.stop.node");
        this.setUsage("/<command>");
        this.setDescription("Stops this specific node on the minecraft network");
        this.plugin = plugin;
    }

    public void execute(CommandSender sender, String[] args) {
        NetCommandBTSC netCommandBTSC = new NetCommandBTSC("stop", plugin.getIP());
        netCommandBTSC.flush();

        plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
            @Override
            public void run() {
                for (ExtendedServerInfo extendedServerInfo : ExtendedServerInfo.getExtendedInfos().values()) {
                    if (extendedServerInfo.getServerInfo().getAddress().getAddress().getHostAddress().equalsIgnoreCase(plugin.getIP())) {
                        NetCommandBTS netCommandBTS = new NetCommandBTS("shutdown", plugin.getIP(), extendedServerInfo.getServerInfo().getName());
                        netCommandBTS.flush();
                    }
                }
                plugin.getProxy().stop();
            }
        }, 10, TimeUnit.SECONDS);
        sender.sendMessage(new TextComponent("Stopping Current Node..."));
    }
}
