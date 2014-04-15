package com.rmb938.bungee.base.command;

import com.rmb938.bungee.base.MN2BungeeBase;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.HashMap;

public abstract class ExtendedCommand extends Command {

    private static HashMap<String, ExtendedCommand> commandMap = new HashMap<>();

    public static HashMap<String, ExtendedCommand> getCommandHashMap() {
        return commandMap;
    }

    public static void registerCommand(Plugin plugin, ExtendedCommand command) {
        commandMap.put(command.getName(), command);
        plugin.getProxy().getPluginManager().registerCommand(plugin, command);
        MN2BungeeBase bungeeBase = (MN2BungeeBase) plugin.getProxy().getPluginManager().getPlugin("MN2BungeeBase");
        bungeeBase.getHelpMap().initializeCommands();
    }

    private String description;
    private String usage;
    private final Plugin plugin;

    public ExtendedCommand(Plugin plugin, String name) {
        super(name);
        this.plugin = plugin;
    }

    public ExtendedCommand(Plugin plugin, String name, String permission) {
        super(name, permission);
        this.plugin = plugin;
    }

    public ExtendedCommand(Plugin plugin, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.plugin = plugin;
    }

    public boolean testPermission(CommandSender sender) {
        return sender.hasPermission(getPermission());
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }
}
