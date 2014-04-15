package com.rmb938.bungee.base.command;

import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.jedis.net.command.bungee.NetCommandBTB;
import com.rmb938.jedis.net.command.bungee.NetCommandBTS;
import com.rmb938.jedis.net.command.bungee.NetCommandBTSC;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.concurrent.TimeUnit;

public class CommandStopNetwork extends ExtendedCommand {

    private final MN2BungeeBase plugin;

    public CommandStopNetwork(MN2BungeeBase plugin) {
        super(plugin, "stopnetwork", "mn2.bungee.stop.network");
        this.setUsage("/<command>");
        this.setDescription("Stops the minecraft network");
        this.plugin = plugin;
    }

    public void execute(CommandSender sender, String[] args) {
        NetCommandBTSC netCommandBTSC = new NetCommandBTSC("stop", "*");
        netCommandBTSC.flush();

        plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
            @Override
            public void run() {
                NetCommandBTS netCommandBTS = new NetCommandBTS("shutdown", plugin.getIP(), "*");
                netCommandBTS.flush();
                NetCommandBTB netCommandBTB = new NetCommandBTB("stop", plugin.getIP(), "*");
                netCommandBTB.flush();
            }
        }, 10, TimeUnit.SECONDS);
        sender.sendMessage(new TextComponent("Stopping Network..."));
    }
}
