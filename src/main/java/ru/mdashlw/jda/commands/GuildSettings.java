package ru.mdashlw.jda.commands;

import java.util.Set;

public interface GuildSettings {

  String getPrefix();

  Set<Long> getBlacklistedChannelIds();
}
