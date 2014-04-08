package com.rmb938.bungee.base.command;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.bungee.base.entity.ExtendedServerInfo;
import com.rmb938.jedis.JedisManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class CommandServer extends Command implements TabExecutor {

    private MN2BungeeBase plugin;

    public CommandServer(MN2BungeeBase plugin) {
        super("server", "bungeecord.command.server");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        Map<String, ServerInfo> servers = plugin.getProxy().getServers();
        if (args.length == 0) {
            ExtendedServerInfo extendedServerInfo = ExtendedServerInfo.getExtendedInfos().get(player.getServer().getInfo().getName());

            Jedis jedis = JedisManager.getJedis();
            while (jedis.setnx("lock." + extendedServerInfo.getServerName()+".key", System.currentTimeMillis() + 30000 + "") == 0) {
                String lock = jedis.get("lock." + extendedServerInfo.getServerName()+".key");
                long time = Long.parseLong(lock != null ? lock : "0");
                if (System.currentTimeMillis() > time) {
                    time = Long.parseLong(jedis.getSet("lock." + extendedServerInfo.getServerName()+".key", System.currentTimeMillis() + 30000 + ""));
                    if (System.currentTimeMillis() < time) {
                        continue;
                    }
                } else {
                    continue;
                }
                break;
            }
            Set<String> keys = jedis.keys("server." + extendedServerInfo.getServerName() + ".*");
            int id = -1;
            for (String key : keys) {
                String uuid = jedis.get(key);
                if (uuid.equals(player.getServer().getInfo().getName())) {
                    id = Integer.parseInt(key.split("\\.")[2]);
                    break;
                }
            }
            jedis.del("lock." + extendedServerInfo.getServerName()+".key");
            JedisManager.returnJedis(jedis);

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
                    jedis = JedisManager.getJedis();
                    while (jedis.setnx("lock." + extendedServerInfo.getServerName()+".key", System.currentTimeMillis() + 30000 + "") == 0) {
                        String lock = jedis.get("lock." + extendedServerInfo.getServerName()+".key");
                        long time = Long.parseLong(lock != null ? lock : "0");
                        if (System.currentTimeMillis() > time) {
                            time = Long.parseLong(jedis.getSet("lock." + extendedServerInfo.getServerName()+".key", System.currentTimeMillis() + 30000 + ""));
                            if (System.currentTimeMillis() < time) {
                                continue;
                            }
                        } else {
                            continue;
                        }
                        break;
                    }
                    keys = jedis.keys("server." + extendedServerInfo.getServerName() + ".*");
                    id = -1;
                    for (String key : keys) {
                        String uuid = jedis.get(key);
                        if (uuid.equals(server.getName())) {
                            id = Integer.parseInt(key.split("\\.")[2]);
                            break;
                        }
                    }
                    jedis.del("lock." + extendedServerInfo.getServerName()+".key");
                    JedisManager.returnJedis(jedis);

                    TextComponent serverTextComponent = new TextComponent(first ? extendedServerInfo.getServerName()+"."+id : ", " + extendedServerInfo.getServerName()+"."+id);
                    serverTextComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("IP: " + extendedServerInfo.getServerInfo().getAddress().getAddress()
                            + " Port: " + extendedServerInfo.getServerInfo().getAddress().getPort()).create()));
                    serverTextComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + server.getName()));
                    serverList.addExtra(serverTextComponent);
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
                    Jedis jedis = JedisManager.getJedis();
                    String uuid = jedis.get("server." + serverName + "." + id);
                    if (uuid != null) {
                        server = servers.get(uuid);
                    }
                    JedisManager.returnJedis(jedis);
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
    public Iterable<String> onTabComplete(final CommandSender sender, String[] args) {
        return (args.length != 0) ? Collections.EMPTY_LIST : Iterables.transform(Iterables.filter(ProxyServer.getInstance().getServers().values(), new Predicate<ServerInfo>() {
            @Override
            public boolean apply(ServerInfo input) {
                return input.canAccess(sender);
            }
        }), new Function<ServerInfo, String>() {
            @Override
            public String apply(ServerInfo input) {
                return input.getName();
            }
        });
    }
}
