package com.rmb938.bungee.base.command;

import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.jedis.net.command.bungee.NetCommandBTSC;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class CommandRefreshServerInfo  extends ExtendedCommand {

    private final MN2BungeeBase plugin;

    public CommandRefreshServerInfo(MN2BungeeBase plugin) {
        super(plugin, "refreshsi", "mn2.bungee.refresh.si");
        this.setUsage("/<command>");
        this.setDescription("Refresh server info in the database");
        this.plugin = plugin;
    }

    public void execute(CommandSender sender, String[] args) {
        if (this.testPermission(sender) == false) {
            return;
        }
        NetCommandBTSC netCommandBTSC = new NetCommandBTSC("refreshServerTypes", "*");
        netCommandBTSC.flush();
        sender.sendMessage(new TextComponent("Refreshing server into..."));
    }
}
