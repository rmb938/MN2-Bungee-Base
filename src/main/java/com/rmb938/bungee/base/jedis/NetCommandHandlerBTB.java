package com.rmb938.bungee.base.jedis;

import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.jedis.net.NetChannel;
import com.rmb938.jedis.net.NetCommandHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
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
                if (toBungee.equalsIgnoreCase(plugin.getPrivateIP()) == false) {
                    return;
                }
            }

            String command = jsonObject.getString("command");
            HashMap<String, Object> objectHashMap = objectToHashMap(jsonObject.getJSONObject("data"));
            switch (command) {
                case "stop":
                    plugin.getProxy().stop();
                    break;
                case "galert":
                    String message = (String) objectHashMap.get("message");
                    plugin.getProxy().broadcast(new TextComponent(ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Global Alert" + ChatColor.GRAY + "]" + ChatColor.RED + message));
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            plugin.getLogger().log(Level.SEVERE, null, e);
        }
    }
}
