package com.rmb938.bungee.base.utils.help;

import java.util.Collection;

public interface HelpMap {
    /**
     * Returns a help topic for a given topic name.
     *
     * @param topicName The help topic name to look up.
     * @return A {@link HelpTopic} object matching the topic name or null if
     * none can be found.
     */
    public HelpTopic getHelpTopic(String topicName);

    /**
     * Returns a collection of all the registered help topics.
     *
     * @return All the registered help topics.
     */
    public Collection<HelpTopic> getHelpTopics();

    /**
     * Adds a topic to the server's help index.
     *
     * @param topic The new help topic to add.
     */
    public void addTopic(HelpTopic topic);

    /**
     * Clears out the contents of the help index. Normally called during
     * server reload.
     */
    public void clear();

}
