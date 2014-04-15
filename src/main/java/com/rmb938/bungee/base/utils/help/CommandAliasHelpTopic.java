package com.rmb938.bungee.base.utils.help;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import org.apache.commons.lang.Validate;

public class CommandAliasHelpTopic extends HelpTopic {

    private final String aliasFor;
    private final HelpMap helpMap;

    public CommandAliasHelpTopic(String alias, String aliasFor, HelpMap helpMap) {
        this.aliasFor = aliasFor.startsWith("/") ? aliasFor : "/" + aliasFor;
        this.helpMap = helpMap;
        this.name = alias.startsWith("/") ? alias : "/" + alias;
        Validate.isTrue(!this.name.equals(this.aliasFor), "Command " + this.name + " cannot be alias for itself");
        this.shortText = ChatColor.YELLOW + "Alias for " + ChatColor.WHITE + this.aliasFor;
    }

    @Override
    public String getFullText(CommandSender forWho) {
        StringBuilder sb = new StringBuilder(shortText);
        HelpTopic aliasForTopic = helpMap.getHelpTopic(aliasFor);
        if (aliasForTopic != null) {
            sb.append("\n");
            sb.append(aliasForTopic.getFullText(forWho));
        }
        return sb.toString();
    }

    @Override
    public boolean canSee(CommandSender commandSender) {
        if (amendedPermission == null) {
            HelpTopic aliasForTopic = helpMap.getHelpTopic(aliasFor);
            if (aliasForTopic != null) {
                return aliasForTopic.canSee(commandSender);
            } else {
                return false;
            }
        } else {
            return commandSender.hasPermission(amendedPermission);
        }
    }
}
