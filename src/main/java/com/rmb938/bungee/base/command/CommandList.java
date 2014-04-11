package com.rmb938.bungee.base.command;

import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.bungee.base.entity.ExtendedServerInfo;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.Collections;

public class CommandList extends Command {

    private final MN2BungeeBase plugin;

    public CommandList(MN2BungeeBase plugin) {
        super("glist", "bungeecord.command.list");
        this.plugin = plugin;
    }

    public void execute(CommandSender sender, String[] args) {
        for (ServerInfo server : plugin.getProxy().getServers().values()) {
            if (server.canAccess(sender)) {
                ArrayList<String> players = new ArrayList<>();
                for (ProxiedPlayer player : server.getPlayers()) {
                    players.add(player.getDisplayName());
                }
                Collections.sort(players, String.CASE_INSENSITIVE_ORDER);

                ExtendedServerInfo extendedServerInfo = ExtendedServerInfo.getExtendedInfos().get(server.getName());

                sender.sendMessage(plugin.getProxy().getTranslation("command_list", new Object[]{extendedServerInfo.getServerName()+"."+extendedServerInfo.getServerId(),
                        extendedServerInfo.getCurrentPlayers(), Util.format(players, ChatColor.RESET + ", ")}));
            }
        }
        int online = 0;
        for (ExtendedServerInfo extendedServerInfo : ExtendedServerInfo.getExtendedInfos().values()) {
            online += extendedServerInfo.getCurrentPlayers();
        }
        sender.sendMessage(plugin.getProxy().getTranslation("total_players", new Object[]{online}));
    }
}
