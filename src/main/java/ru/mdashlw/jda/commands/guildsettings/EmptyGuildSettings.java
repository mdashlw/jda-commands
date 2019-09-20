package ru.mdashlw.jda.commands.guildsettings;

import java.util.Collections;
import java.util.Set;
import ru.mdashlw.jda.commands.GuildSettings;
import ru.mdashlw.jda.commands.handler.CommandHandler;

// TODO Move to other package
public final class EmptyGuildSettings implements GuildSettings {

  private final String prefix;

  public EmptyGuildSettings(final CommandHandler handler) {
    this.prefix = handler.getPrefix();
  }

  @Override
  public String getPrefix() {
    return this.prefix;
  }

  @Override
  public Set<Long> getBlacklistedChannelIds() {
    return Collections.emptySet();
  }
}
