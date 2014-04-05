package com.rmb938.bungee.base.command;

import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.jedis.net.command.bungee.NetCommandBTB;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class CommandMaintenance extends Command {

    private MN2BungeeBase plugin;

    public CommandMaintenance(MN2BungeeBase plugin) {
        super("maintenance", "mn2.bungee.maintenance");
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        String action = plugin.isMaintenance() == false ? "on" : "off";
        commandSender.sendMessage(new TextComponent("Maintenance Mode has been turned "+action));
        NetCommandBTB netCommandBTB = new NetCommandBTB("maintenance", "", "*");
        netCommandBTB.flush();
    }
}
