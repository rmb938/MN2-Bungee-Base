package com.rmb938.bungee.base.jedis;

import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.jedis.net.NetChannel;
import com.rmb938.jedis.net.NetCommandHandler;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

public class NetCommandHandlerBTB extends NetCommandHandler {

    private final MN2BungeeBase plugin;

    public NetCommandHandlerBTB(MN2BungeeBase plugin) {
        NetCommandHandler.addHandler(NetChannel.BUNGEE_TO_BUNGEE, this);
        this.plugin = plugin;
    }

    @Override
    public void handle(JSONObject jsonObject) {
        try {
            String fromBungee = jsonObject.getString("from");
            String toBungee = jsonObject.getString("to");

            if (toBungee.equalsIgnoreCase("*") == false) {
                if (toBungee.equalsIgnoreCase(plugin.getIP()) == false) {
                    return;
                }
            }

            String command = jsonObject.getString("command");
            HashMap<String, Object> objectHashMap = objectToHashMap(jsonObject.getJSONObject("data"));
            switch (command) {
                case "maintenance":
                    plugin.setMaintenance(true);
                    Iterator<ProxiedPlayer> players = plugin.getProxy().getPlayers().iterator();
                    while(players.hasNext()) {
                        ProxiedPlayer player = players.next();
                        player.disconnect(new TextComponent("Server is going down for maintenance."));
                    }
                    break;
                default:
                    plugin.getLogger().info("Unknown BTB Command MN2BukkitBase " + command);
            }
        } catch (JSONException e) {
            plugin.getLogger().log(Level.SEVERE, null, e);
        }
    }
}
