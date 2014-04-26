package com.rmb938.bungee.base.command;

import com.google.common.collect.ImmutableSet;
import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.bungee.base.entity.ExtendedServerInfo;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CommandSend extends ExtendedCommand implements TabExecutor {

    private final MN2BungeeBase plugin;

    public CommandSend(MN2BungeeBase plugin) {
        super(plugin, "send", "bungeecord.command.send");
        this.setUsage("/<player|all|current> <target>");
        this.setDescription("Sends a player to a specific server.");
        this.plugin = plugin;
    }

    public void execute(CommandSender sender, String[] args) {
        if (this.testPermission(sender) == false) {
            return;
        }
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Not enough arguments, usage: /send <player|all|current> <target>");
            return;
        }
        String serverInfo = args[1];
        int serverId = -1;
        String[] split = serverInfo.split("\\.");
        if (split.length == 2) {
            try {
                serverId = Integer.parseInt(split[1]);
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Target must be server type or serverType.id");
                return;
            }
        }
        ServerInfo target = null;
        ArrayList<ServerInfo> servers = new ArrayList<>();
        for (ExtendedServerInfo extendedServerInfo : ExtendedServerInfo.getExtendedInfos().values()) {
            if (extendedServerInfo.getServerName().equalsIgnoreCase(split[0]) == false) {
                continue;
            }
            if (extendedServerInfo.getServerId() != serverId && serverId != -1) {
                continue;
            }
            servers.add(extendedServerInfo.getServerInfo());
        }
        if (servers.isEmpty() == false) {
            int random = (int) (Math.random() * servers.size());
            target = servers.get(random);
        }
        if (target == null) {
            sender.sendMessage(ProxyServer.getInstance().getTranslation("no_server", new Object[0]));
            return;
        }
        if (args[0].equalsIgnoreCase("all")) {
            for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
                summon(p, target, sender);
            }
        } else if (args[0].equalsIgnoreCase("current")) {
            if (!(sender instanceof ProxiedPlayer)) {
                sender.sendMessage(ChatColor.RED + "Only in game players can use this command");
                return;
            }
            ProxiedPlayer player = (ProxiedPlayer) sender;
            for (ProxiedPlayer p : player.getServer().getInfo().getPlayers()) {
                summon(p, target, sender);
            }
        } else {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[0]);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "That player is not online");
                return;
            }
            summon(player, target, sender);
        }
        sender.sendMessage(ChatColor.GREEN + "Successfully summoned player(s)");
    }

    private void summon(ProxiedPlayer player, ServerInfo target, CommandSender sender) {
        if ((player.getServer() != null) && (!player.getServer().getInfo().equals(target))) {
            player.connect(target);
            player.sendMessage(ChatColor.GOLD + "Summoned to " + target.getName() + " by " + sender.getName());
        }
    }

    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if ((args.length > 2) || (args.length == 0)) {
            return ImmutableSet.of();
        }
        Set<String> matches = new HashSet<>();
        String search;
        if (args.length == 1) {
            search = args[0].toLowerCase();
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                if (player.getName().toLowerCase().startsWith(search)) {
                    matches.add(player.getName());
                }
            }
            if ("all".startsWith(search)) {
                matches.add("all");
            }
            if ("current".startsWith(search)) {
                matches.add("current");
            }
        } else {
            search = args[1].toLowerCase();
            for (String server : ProxyServer.getInstance().getServers().keySet()) {
                if (server.toLowerCase().startsWith(search)) {
                    matches.add(server);
                }
            }
        }
        return matches;
    }
}
