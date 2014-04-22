package com.rmb938.bungee.base.listeners;

import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.bungee.base.entity.ExtendedServerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;

public class PluginListener implements Listener {

    private final MN2BungeeBase plugin;

    public PluginListener(MN2BungeeBase plugin) {
        this.plugin = plugin;
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.getReceiver() instanceof ProxiedPlayer == false) {
            return;
        }
        if (event.getTag().equalsIgnoreCase("BungeeCord")) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));
            try {
                String subchannel = in.readUTF();

                if (subchannel.equalsIgnoreCase("connect")) {
                    String[] info = in.readUTF().split("\\.");
                    if (info.length == 1) {
                        ArrayList<ServerInfo> serverInfos = new ArrayList<>();
                        for (ExtendedServerInfo extendedServerInfo : ExtendedServerInfo.getExtendedInfos(info[0])) {
                            if (extendedServerInfo.getFree() > 1) {
                                serverInfos.add(extendedServerInfo.getServerInfo());
                            }
                        }
                        if (serverInfos.isEmpty() == false) {
                            int random = (int) (Math.random() * serverInfos.size());
                            ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
                            player.connect(serverInfos.get(random));
                        }
                    } else if (info.length == 2) {
                        int id = Integer.parseInt(info[1]);
                        for (ExtendedServerInfo extendedServerInfo : ExtendedServerInfo.getExtendedInfos(info[0])) {
                            if (extendedServerInfo.getServerId() == id) {
                                ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
                                player.connect(extendedServerInfo.getServerInfo());
                                break;
                            }
                        }
                    }
                    event.setCancelled(true);
                } else if (subchannel.equalsIgnoreCase("typeAmount")) {
                    String type = in.readUTF();
                    int amount = ExtendedServerInfo.getExtendedInfos(type).size();

                    ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(byteArray);

                    out.writeUTF("typeAmount");
                    out.writeUTF(type);
                    out.writeInt(amount);
                    out.flush();

                    ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
                    player.getServer().sendData("BungeeCord", byteArray.toByteArray());
                    event.setCancelled(true);
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, null, e);
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, null, e);
                }
            }
        }
    }

}
