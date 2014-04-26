package com.rmb938.bungee.base.command;

import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.jedis.net.command.bungee.NetCommandBTB;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class CommandGAlert extends ExtendedCommand {

    private final MN2BungeeBase plugin;

    public CommandGAlert(MN2BungeeBase plugin) {
        super(plugin, "galert", "mn2.galert");
        this.setUsage("/<command> [message]");
        this.setDescription("Sends a message to the whole network");
        this.plugin = plugin;
    }

    public void execute(CommandSender sender, String[] args) {
        if (this.testPermission(sender) == false) {
            return;
        }
        if (args.length == 0) {
            sender.sendMessage(new TextComponent(ChatColor.RED+"Usage: /galert [message]"));
            return;
        }
        String message = "";
        for (String s : args) {
            message += s+" ";
        }
        message = message.trim();
        NetCommandBTB netCommandBTB = new NetCommandBTB("galert", plugin.getPrivateIP(), "*");
        netCommandBTB.addArg("message", message);
        netCommandBTB.flush();
    }
}
