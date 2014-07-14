package com.rmb938.bungee.base.command;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.bungee.base.entity.ExtendedServerInfo;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class CommandServer extends ExtendedCommand implements TabExecutor {

    private MN2BungeeBase plugin;

    public CommandServer(MN2BungeeBase plugin) {
        super(plugin, "server", "bungeecord.command.server");
        this.setUsage("/<command> [serverName]");
        this.setDescription("Shows a list and teleports to servers on the network");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (this.testPermission(sender) == false) {
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        Map<String, ServerInfo> servers = plugin.getProxy().getServers();
        if (args.length == 0) {
            ExtendedServerInfo extendedServerInfo = ExtendedServerInfo.getExtendedInfos().get(player.getServer().getInfo().getName());

            int id = extendedServerInfo.getServerId();
            TextComponent currentServer = new TextComponent(plugin.getProxy().getTranslation("current_server") + extendedServerInfo.getServerName()+"."+id);
            currentServer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("IP: " + extendedServerInfo.getServerInfo().getAddress().getAddress()
                    + " Port: " + extendedServerInfo.getServerInfo().getAddress().getPort()).create()));
            player.sendMessage(currentServer);
            TextComponent serverList = new TextComponent(plugin.getProxy().getTranslation("server_list"));
            serverList.setColor(ChatColor.GOLD);
            boolean first = true;
            for (ServerInfo server : servers.values()) {
                if (server.canAccess(player)) {
                    extendedServerInfo = ExtendedServerInfo.getExtendedInfos().get(server.getName());
                    if (extendedServerInfo != null) {
                        id = extendedServerInfo.getServerId();

                        TextComponent serverTextComponent = new TextComponent(first ? extendedServerInfo.getServerName() + "." + id : ", " + extendedServerInfo.getServerName() + "." + id);
                        serverTextComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("IP: " + extendedServerInfo.getServerInfo().getAddress().getAddress()
                                + " Port: " + extendedServerInfo.getServerInfo().getAddress().getPort()).create()));
                        serverTextComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + server.getName()));
                        serverList.addExtra(serverTextComponent);
                    }
                    first = false;

                }
            }
            player.sendMessage(serverList);
        } else {
            String serverName = args[0];
            ServerInfo server = servers.get(serverName);
            if (server == null) {
                String[] info = serverName.split("\\.");
                if (info.length == 2) {
                    serverName = info[0];
                    int id = Integer.parseInt(info[1]);
                    for (ExtendedServerInfo extendedServerInfo : ExtendedServerInfo.getExtendedInfos(info[0])) {
                        if (extendedServerInfo.getServerId() == id) {
                            server = extendedServerInfo.getServerInfo();
                            break;
                        }
                    }
                } else if (info.length == 1) {
                    ArrayList<ServerInfo> serverInfos = new ArrayList<>();
                    for (ExtendedServerInfo extendedServerInfo : ExtendedServerInfo.getExtendedInfos(info[0])) {
                        if (extendedServerInfo.getFree() > 1) {
                            serverInfos.add(extendedServerInfo.getServerInfo());
                        }
                    }
                    if (serverInfos.isEmpty() == false) {
                        int random = (int) (Math.random() * serverInfos.size());
                        server = serverInfos.get(random);
                    }
                }
            }
            if (server == null) {
                player.sendMessage(plugin.getProxy().getTranslation("no_server"));
            } else if (!server.canAccess(player)) {
                player.sendMessage(plugin.getProxy().getTranslation("no_server_permission"));
            } else {
                player.connect(server);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<String> onTabComplete(final CommandSender sender, String[] args) {
        return (args.length != 0) ? Collections.EMPTY_LIST : Iterables.transform(Iterables.filter(ExtendedServerInfo.getExtendedInfos().values(), new Predicate<ExtendedServerInfo>() {
            @Override
            public boolean apply(ExtendedServerInfo input) {
                return input.getServerInfo().canAccess(sender);
            }
        }), new Function<ExtendedServerInfo, String>() {
            @Override
            public String apply(ExtendedServerInfo input) {
                return input.getServerName()+"."+input.getServerId();
            }
        });
    }
}
