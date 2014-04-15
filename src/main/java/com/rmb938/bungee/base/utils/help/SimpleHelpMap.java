package com.rmb938.bungee.base.utils.help;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.rmb938.bungee.base.MN2BungeeBase;
import com.rmb938.bungee.base.command.ExtendedCommand;
import net.md_5.bungee.api.plugin.Command;

import java.util.*;

public class SimpleHelpMap implements HelpMap {

    private HelpTopic defaultTopic;
    private final Map<String, HelpTopic> helpTopics;
    private final MN2BungeeBase plugin;

    public SimpleHelpMap(MN2BungeeBase plugin) {
        this.helpTopics = new TreeMap<>(HelpTopicComparator.topicNameComparatorInstance()); // Using a TreeMap for its explicit sorting on key
        this.plugin = plugin;

        Predicate indexFilter = Predicates.not(Predicates.instanceOf(CommandAliasHelpTopic.class));

        this.defaultTopic = new IndexHelpTopic("Index", null, null, Collections2.filter(helpTopics.values(), indexFilter), "Use /bhelp [n] to get page n of help.");
    }

    public synchronized HelpTopic getHelpTopic(String topicName) {
        if (topicName.equals("")) {
            return defaultTopic;
        }

        if (helpTopics.containsKey(topicName)) {
            return helpTopics.get(topicName);
        }

        return null;
    }

    public Collection<HelpTopic> getHelpTopics() {
        return helpTopics.values();
    }

    public synchronized void addTopic(HelpTopic topic) {
        // Existing topics take priority
        if (!helpTopics.containsKey(topic.getName())) {
            helpTopics.put(topic.getName(), topic);
        }
    }

    public synchronized void clear() {
        helpTopics.clear();
    }

    /**
     * Processes all the commands registered in the server and creates help topics for them.
     */
    public synchronized void initializeCommands() {

        // Initialize help topics from the server's command map
        outer:
        for (ExtendedCommand command : ExtendedCommand.getCommandHashMap().values()) {
            addTopic(new GenericCommandHelpTopic(command));
        }

        // Initialize command alias help topics
        for (ExtendedCommand command : ExtendedCommand.getCommandHashMap().values()) {
            for (String alias : command.getAliases()) {
                addTopic(new CommandAliasHelpTopic("/" + alias, "/" + command.getName(), this));
            }
        }

        // Add alias sub-index
        Collection<HelpTopic> filteredTopics = Collections2.filter(helpTopics.values(), Predicates.instanceOf(CommandAliasHelpTopic.class));
        if (!filteredTopics.isEmpty()) {
            addTopic(new IndexHelpTopic("Aliases", "Lists command aliases", null, filteredTopics));
        }

        // Initialize plugin-level sub-topics
        Map<String, Set<HelpTopic>> pluginIndexes = new HashMap<>();
        fillPluginIndexes(pluginIndexes, ExtendedCommand.getCommandHashMap().values());

        for (Map.Entry<String, Set<HelpTopic>> entry : pluginIndexes.entrySet()) {
            addTopic(new IndexHelpTopic(entry.getKey(), "All commands for " + entry.getKey(), null, entry.getValue(), "Below is a list of all " + entry.getKey() + " commands:"));
        }
    }

    private void fillPluginIndexes(Map<String, Set<HelpTopic>> pluginIndexes, Collection<? extends ExtendedCommand> commands) {
        for (ExtendedCommand command : commands) {
            String pluginName = getCommandPluginName(command);
            plugin.getLogger().info("Command "+command.getName()+" Plugin: "+pluginName);
            if (pluginName != null) {
                HelpTopic topic = getHelpTopic("/" + command.getName());
                if (topic != null) {
                    if (!pluginIndexes.containsKey(pluginName)) {
                        pluginIndexes.put(pluginName, new TreeSet<>(HelpTopicComparator.helpTopicComparatorInstance())); //keep things in topic order
                    }
                    pluginIndexes.get(pluginName).add(topic);
                }
            }
        }
    }

    private String getCommandPluginName(Command command) {
        if (command instanceof ExtendedCommand) {
            return ((ExtendedCommand)command).getPlugin().getDescription().getName();
        }
        return null;
    }

    private class IsCommandTopicPredicate implements Predicate<HelpTopic> {

        public boolean apply(HelpTopic topic) {
            return topic.getName().charAt(0) == '/';
        }
    }
}
