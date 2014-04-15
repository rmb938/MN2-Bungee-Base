package com.rmb938.bungee.base.utils.help;

import com.rmb938.bungee.base.command.ExtendedCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

public class GenericCommandHelpTopic extends HelpTopic {

    private final Command command;

    public GenericCommandHelpTopic(ExtendedCommand command) {
        this.command = command;

        if (command.getName().startsWith("/")) {
            name = command.getName();
        } else {
            name = "/" + command.getName();
        }

        // The short text is the first line of the description
        int i = command.getDescription().indexOf("\n");
        if (i > 1) {
            shortText = command.getDescription().substring(0, i - 1);
        } else {
            shortText = command.getDescription();
        }

        // Build full text
        StringBuffer sb = new StringBuffer();
        sb.append(ChatColor.GOLD);
        sb.append("Description: ");
        sb.append(ChatColor.WHITE);
        sb.append(command.getDescription());

        sb.append("\n");
        sb.append(ChatColor.GOLD);
        sb.append("Usage: ");
        sb.append(ChatColor.WHITE);
        sb.append(command.getUsage().replace("<command>", name.substring(1)));

        if (command.getAliases().length > 0) {
            sb.append("\n");
            sb.append(ChatColor.GOLD);
            sb.append("Aliases: ");
            sb.append(ChatColor.WHITE);
            sb.append(ChatColor.WHITE);
            sb.append(StringUtils.join(Arrays.asList(command.getAliases()), ", "));
        }
        fullText = sb.toString();
    }

    public boolean canSee(CommandSender sender) {
        if (amendedPermission != null) {
            return sender.hasPermission(amendedPermission);
        } else {
            return sender.hasPermission(command.getPermission());
        }
    }
}
