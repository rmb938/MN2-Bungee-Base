package com.rmb938.bungee.base.command;

import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.jedis.net.command.bungee.NetCommandBTS;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandStop extends Command {

    private MN2BungeeBase plugin;

    public CommandStop(MN2BungeeBase plugin) {
        super("stop", "mn2.bungee.stop");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) commandSender;
            ServerInfo serverInfo = player.getServer().getInfo();
            NetCommandBTS netCommandBTS = new NetCommandBTS("shutdown", plugin.getIP(), serverInfo.getName());
            netCommandBTS.flush();
        } else {
            commandSender.sendMessage(new TextComponent("You must be a player to shutdown a server."));
        }
    }
}
