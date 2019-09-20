package ru.mdashlw.jda.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import ru.mdashlw.jda.commands.handler.CommandHandler;
import ru.mdashlw.jda.commands.util.crossplatform.impl.CrossplatformEmbed;
import ru.mdashlw.jda.commands.util.crossplatform.impl.CrossplatformMessage;
import ru.mdashlw.jda.commands.util.crossplatform.impl.CrossplatformText;

public abstract class Command {

  private final List<Method> executors;
  private Map<String, SubCommand> subCommands;

  public Command() {
    this.executors = Arrays.stream(this.getClass().getMethods())
        .filter(method -> method.isAnnotationPresent(Executor.class))
        .sorted(Comparator.comparingInt(Method::getParameterCount).reversed())
        .collect(Collectors.toList());
  }

  public abstract Category getCategory();

  public abstract String getName();

  public String getQualifiedName() {
    return this.getName();
  }

  public List<String> getAliases() {
    return Collections.emptyList();
  }

  public String getDescription() {
    return "<no description>";
  }

  public String getLongDescription() {
    return this.getDescription();
  }

  public String getUsage() {
    return "";
  }

  public final String getQualifiedUsage() {
    final String qualifiedName = this.getQualifiedName();
    final String usage = this.getUsage();
    final String qualifiedUsage = qualifiedName + ' ' + usage;

    return qualifiedUsage.strip();
  }

  public List<String> getExamples() {
    return Collections.emptyList();
  }

  public final List<String> getQualifiedExamples() {
    final List<String> examples = this.getExamples();

    if (examples.isEmpty()) {
      return examples;
    }

    final String qualifiedName = this.getQualifiedName();

    return examples.stream()
        .map(example -> qualifiedName + ' ' + example)
        .map(String::strip)
        .collect(Collectors.toList());
  }

  public EnumSet<Permission> getMemberPermissions() {
    return this.getCategory().getMemberPermissions();
  }

  public EnumSet<Permission> getBotPermissions() {
    return this.getCategory().getBotPermissions();
  }

  public boolean requiresSendMessagesPermission() {
    return true;
  }

  public boolean requiresEmbedLinksPermission() {
    return true;
  }

  public boolean isShownInHelp() {
    return this.getCategory().isShownInHelp();
  }

  public boolean hasAccess(final Event event) {
    return this.getCategory().hasAccess(event);
  }

  public final void registerSubCommand(final SubCommand command) {
    if (this.subCommands == null) {
      this.subCommands = new HashMap<>();
    }

    this.subCommands.put(command.getName().toLowerCase(Locale.ENGLISH), command);
    command.getAliases()
        .forEach(alias -> this.subCommands.put(alias.toLowerCase(Locale.ENGLISH), command));
  }

  public final SubCommand getSubCommand(final String name) {
    return this.subCommands != null ? this.subCommands.get(name.toLowerCase(Locale.ENGLISH)) : null;
  }

  public final Method getExecutor(final int args) {
    return this.executors.stream()
        .filter(method -> method.getParameterCount() - 1 <= args)
        .findFirst().orElse(null);
  }

  public final Event createEvent(final CommandHandler handler, final GuildSettings guildSettings,
      final Message message) {
    return new Event(handler, guildSettings, message);
  }

  public final Event copyEvent(final Event event) {
    return new Event(event.getHandler(), event.getGuildSettings(), event.getMessage());
  }

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  protected @interface Executor {

  }

  public final class Event {

    private final CommandHandler handler;
    private final GuildSettings guildSettings;
    private final Message message;

    public Event(final CommandHandler handler, final GuildSettings guildSettings,
        final Message message) {
      this.handler = handler;
      this.guildSettings = guildSettings;
      this.message = message;
    }

    public MessageAction reply(final CharSequence text) {
      return this.getChannel().sendMessage(text);
    }

    public MessageAction reply(final MessageEmbed embed) {
      return this.getChannel().sendMessage(embed);
    }

    public MessageAction reply(final Message message) {
      return this.getChannel().sendMessage(message);
    }

    public MessageAction reply(final CrossplatformText text) {
      return this.reply(text.get(this.getMember()));
    }

    public MessageAction reply(final CrossplatformEmbed embed) {
      return this.reply(embed.get(this.getMember()));
    }

    public MessageAction reply(final CrossplatformMessage message) {
      return this.reply(message.get(this.getMember()));
    }

    public MessageAction replyHelp() {
      return this.handler.getReplies().replyHelp(Command.this, this);
    }

    public MessageAction replyError(final String message) {
      return this.handler.getReplies().replyError(Command.this, this, message);
    }

    public JDA getJDA() {
      return this.message.getJDA();
    }

    public Guild getGuild() {
      return this.message.getGuild();
    }

    public TextChannel getChannel() {
      return this.message.getTextChannel();
    }

    public Member getMember() {
      return this.message.getMember();
    }

    public User getUser() {
      return this.message.getAuthor();
    }

    public CommandHandler getHandler() {
      return this.handler;
    }

    public GuildSettings getGuildSettings() {
      return this.guildSettings;
    }

    public Message getMessage() {
      return this.message;
    }
  }
}
