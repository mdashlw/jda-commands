package ru.mdashlw.jda.commands;

import net.dv8tion.jda.api.entities.Guild;

@FunctionalInterface
public interface GuildSettingsProvider {

  GuildSettings provide(Guild guild);
}
