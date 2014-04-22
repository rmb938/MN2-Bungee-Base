package com.rmb938.bungee.base.jedis;

import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.bungee.base.entity.ExtendedServerInfo;
import com.rmb938.jedis.net.NetChannel;
import com.rmb938.jedis.net.NetCommandHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.logging.Level;

public class NetCommandHandlerSCTB extends NetCommandHandler {

    private final MN2BungeeBase plugin;

    public NetCommandHandlerSCTB(MN2BungeeBase plugin) {
        NetCommandHandler.addHandler(NetChannel.SERVER_CONTROLLER_TO_BUNGEE, this);
        this.plugin = plugin;
    }

    @Override
    public void handle(JSONObject jsonObject) {
        try {
            String fromServerController = jsonObject.getString("from");
            String toBungee = jsonObject.getString("to");

            if (toBungee.equalsIgnoreCase("*") == false) {
                if (toBungee.equalsIgnoreCase(plugin.getPrivateIP()) == false) {
                    return;
                }
            }

            String command = jsonObject.getString("command");
            HashMap<String, Object> objectHashMap = objectToHashMap(jsonObject.getJSONObject("data"));
            switch (command) {
                case "shutdown":
                    plugin.getProxy().stop();
                    break;
                case "removeServer":
                    String serverUUID = (String) objectHashMap.get("serverUUID");
                    plugin.getProxy().getServers().remove(serverUUID);
                    ExtendedServerInfo.getExtendedInfos().remove(serverUUID);
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            plugin.getLogger().log(Level.SEVERE, null, e);
        }
    }
}
