package ru.mdashlw.jda.commands.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.EventListener;
import ru.mdashlw.jda.commands.Command;
import ru.mdashlw.jda.commands.Command.Event;
import ru.mdashlw.jda.commands.Context;
import ru.mdashlw.jda.commands.ExceptionHandler;
import ru.mdashlw.jda.commands.GuildSettings;
import ru.mdashlw.jda.commands.GuildSettingsProvider;
import ru.mdashlw.jda.commands.Replies;
import ru.mdashlw.jda.commands.SubCommand;
import ru.mdashlw.jda.commands.UncaughtExceptionHandler;
import ru.mdashlw.jda.commands.contexts.BooleanContext;
import ru.mdashlw.jda.commands.contexts.IntContext;
import ru.mdashlw.jda.commands.contexts.StringContext;
import ru.mdashlw.jda.commands.exceptionhandlers.DefaultUncaughtExceptionHandler;
import ru.mdashlw.jda.commands.guildsettings.EmptyGuildSettings;
import ru.mdashlw.jda.commands.replies.DefaultReplies;
import ru.mdashlw.jda.commands.util.StringUtils;

// TODO Cleanup
public final class CommandHandler implements EventListener {

  private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

  private final String prefix;
  private final long ownerId;
  private final GuildSettingsProvider guildSettingsProvider;
  private final UncaughtExceptionHandler uncaughtExceptionHandler;
  private final Replies replies;
  private final Map<? super String, ? extends Command> commands;
  private final List<? extends Context<?>> contexts;
  private final List<ExceptionHandler<Throwable>> exceptionHandlers;

  public CommandHandler(final String prefix, final long ownerId,
      final GuildSettingsProvider guildSettingsProvider,
      final UncaughtExceptionHandler uncaughtExceptionHandler, final Replies replies,
      final Map<? super String, ? extends Command> commands,
      final List<? extends Context<?>> contexts,
      final List<ExceptionHandler<Throwable>> exceptionHandlers) {
    this.prefix = prefix;
    this.ownerId = ownerId;
    this.guildSettingsProvider = guildSettingsProvider;
    this.uncaughtExceptionHandler =
        uncaughtExceptionHandler == null ? new DefaultUncaughtExceptionHandler()
            : uncaughtExceptionHandler;
    this.replies = replies == null ? new DefaultReplies() : replies;
    this.commands = commands;
    this.contexts = contexts;
    this.exceptionHandlers = exceptionHandlers;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public void onEvent(@Nonnull final GenericEvent event) {
    if (event instanceof GuildMessageReceivedEvent) {
      this.handleEvent((GuildMessageReceivedEvent) event);
    }
  }

  private void handleEvent(final GuildMessageReceivedEvent event) {
    final Guild guild = event.getGuild();
    final TextChannel channel = event.getChannel();
    final Member member = event.getMember();
    final User user = event.getAuthor();
    final Message message = event.getMessage();
    String content = message.getContentRaw();

    if (member == null || user.isBot() || user.isFake() || content.isBlank()) {
      return;
    }

    final GuildSettings guildSettings = this.getGuildSettings(guild);
    final String prefix = guildSettings.getPrefix();
    final Set<Long> blacklistedChannelIds = guildSettings.getBlacklistedChannelIds();

    content = StringUtils.stripExtraSpaces(content.strip());

    if (!content.toLowerCase(Locale.ENGLISH).startsWith(prefix.toLowerCase(Locale.ENGLISH))) {
      return;
    }

    content = content.substring(prefix.length()).strip();

    String[] args = content.split(" ");
    final Command command = this.getCommand(args[0]);

    if (command == null) {
      return;
    }

    final Member selfMember = guild.getSelfMember();

    if (command.requiresSendMessagesPermission()
        && !selfMember.hasPermission(channel, Permission.MESSAGE_WRITE)) {
      return;
    }

    final Command.Event commandEvent = command.createEvent(this, guildSettings, message);

    if (blacklistedChannelIds.contains(channel.getIdLong())
        && !member.hasPermission(channel, Permission.MANAGE_SERVER)) {
      try {
        message.delete().queue();
      } catch (final InsufficientPermissionException ignored) {
      }

      this.replies.replyBlacklistedChannel(command, commandEvent)
          .queue(bcMessage -> bcMessage.delete().queueAfter(5, TimeUnit.SECONDS));
      return;
    }

    if (command.requiresEmbedLinksPermission()
        && !selfMember.hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)) {
      this.replies.replyNoEmbedLinksPermission(command, commandEvent).queue();
      return;
    }

    args = Arrays.copyOfRange(args, 1, args.length);

    this.execute(command, commandEvent, args);
  }

  private void execute(final Command command, final Command.Event event, final String[] args) {
    if (!this.checkAccess(command, event)) {
      return;
    }

    if (args.length == 0) {
      this.execute(command, event, command.getExecutor(0), args);
      return;
    }

    final SubCommand subCommand = command.getSubCommand(args[0]);

    if (subCommand != null) {
      final Event newEvent = subCommand.copyEvent(event);
      final String[] newArgs = Arrays.copyOfRange(args, 1, args.length);

      this.execute(subCommand, newEvent, newArgs);
      return;
    }

    this.execute(command, event, command.getExecutor(args.length), args);
  }

  private boolean checkAccess(final Command command, final Command.Event event) {
    final TextChannel channel = event.getChannel();
    final User user = event.getUser();

    if (!command.hasAccess(event) && this.ownerId != user.getIdLong()) {
      this.replies.replyNoAccess(command, event).queue();
      return false;
    }

    final Member member = event.getMember();
    final EnumSet<Permission> userPermissions = command.getMemberPermissions();

    if (!member.hasPermission(channel, userPermissions)) {
      this.replies.replyNoMemberPermissions(command, event, userPermissions).queue();
      return false;
    }

    final Member selfMember = event.getGuild().getSelfMember();
    final EnumSet<Permission> botPermissions = command.getBotPermissions();

    if (!selfMember.hasPermission(channel, botPermissions)) {
      this.replies.replyNoBotPermissions(command, event, botPermissions).queue();
      return false;
    }

    return true;
  }

  private void execute(final Command command, final Command.Event event, final Method method,
      final String[] args) {
    if (method == null) {
      this.replies.replyHelp(command, event).queue();
      return;
    }

    final Parameter[] parameters = method.getParameters();
    final List<Object> arguments = this
        .parseArguments(event, Arrays.copyOfRange(parameters, 1, parameters.length), args);

    if (arguments == null) {
      return;
    }

    try {
      method.invoke(command, arguments.toArray(EMPTY_OBJECT_ARRAY));
    } catch (final InvocationTargetException exception) {
      this.handleException(command, event, exception.getCause());
    } catch (final Throwable exception) {
      this.handleException(command, event, exception);
    }
  }

  private List<Object> parseArguments(final Command.Event event, final Parameter[] parameters,
      final String[] args) {
    final List<Object> arguments = new ArrayList<>(parameters.length + 1);

    arguments.add(event);

    for (int i = 0; i < parameters.length; i++) {
      final Parameter parameter = parameters[i];
      final String text = String.join(" ", Arrays.copyOfRange(args, i, args.length));
      final String arg = args[i];

      final Context<?> context = this.getContext(parameter.getType());
      final Object value = context.resolve(event, i, parameter, text, arg);

      if (value == null) {
        return null;
      }

      arguments.add(value);
    }

    return arguments;
  }

  private <T extends Throwable> void handleException(final Command command,
      final Command.Event event, final T exception) {
    final ExceptionHandler<? super Throwable> exceptionHandler = this
        .getExceptionHandler(exception.getClass());

    exceptionHandler.handle(command, event, exception);
  }

  public GuildSettings getGuildSettings(final Guild guild) {
    GuildSettings guildSettings = null;

    if (this.guildSettingsProvider != null) {
      guildSettings = this.guildSettingsProvider.provide(guild);
    }

    return Objects.requireNonNullElseGet(guildSettings, () -> new EmptyGuildSettings(this));
  }

  public Command getCommand(final String name) {
    return this.commands.get(name.toLowerCase(Locale.ENGLISH));
  }

  public Context<?> getContext(final Class<?> clazz) {
    return this.contexts.stream()
        .filter(context -> context.getType().isAssignableFrom(clazz))
        .findFirst().orElseThrow(() ->
            new IllegalStateException("No command context for type " + clazz.getPackageName()));
  }

  public ExceptionHandler<? super Throwable> getExceptionHandler(
      final Class<? extends Throwable> type) {
    return this.exceptionHandlers.stream()
        .filter(handler -> handler.getType().isAssignableFrom(type))
        .findFirst().orElse(this.uncaughtExceptionHandler);
  }

  public String getPrefix() {
    return this.prefix;
  }

  public long getOwnerId() {
    return this.ownerId;
  }

  public GuildSettingsProvider getGuildSettingsProvider() {
    return this.guildSettingsProvider;
  }

  public UncaughtExceptionHandler getUncaughtExceptionHandler() {
    return this.uncaughtExceptionHandler;
  }

  public Replies getReplies() {
    return this.replies;
  }

  public Map<? super String, ? extends Command> getCommands() {
    return this.commands;
  }

  public List<? extends Context<?>> getContexts() {
    return this.contexts;
  }

  public List<ExceptionHandler<Throwable>> getExceptionHandlers() {
    return this.exceptionHandlers;
  }

  public static final class Builder {

    private final Map<? super String, Command> commands = new HashMap<>();
    private final List<Context<?>> contexts = new ArrayList<>();
    private final List<ExceptionHandler<Throwable>> exceptionHandlers = new ArrayList<>();
    private String prefix;
    private long ownerId;
    private GuildSettingsProvider guildSettingsProvider;
    private UncaughtExceptionHandler uncaughtExceptionHandler;
    private Replies replies;

    public Builder prefix(final String prefix) {
      this.prefix = prefix;
      return this;
    }

    public Builder ownerId(final long ownerId) {
      this.ownerId = ownerId;
      return this;
    }

    public Builder guildSettingsProvider(final GuildSettingsProvider guildSettingsProvider) {
      this.guildSettingsProvider = guildSettingsProvider;
      return this;
    }

    public Builder uncaughtExceptionHandler(
        final UncaughtExceptionHandler uncaughtExceptionHandler) {
      this.uncaughtExceptionHandler = uncaughtExceptionHandler;
      return this;
    }

    public Builder replies(final Replies replies) {
      this.replies = replies;
      return this;
    }

    public Builder commands(final Command... commands) {
      for (final Command command : commands) {
        this.commands.put(command.getName().toLowerCase(Locale.ENGLISH), command);
        command.getAliases()
            .forEach(alias -> this.commands.put(alias.toLowerCase(Locale.ENGLISH), command));
      }

      return this;
    }

    public Builder defaultContexts() {
      // TODO Create more contexts
      return this.contexts(new StringContext(), new BooleanContext(), new IntContext());
    }

    public Builder contexts(final Context<?>... contexts) {
      Collections.addAll(this.contexts, contexts);
      return this;
    }

    @SuppressWarnings("unchecked")
    public Builder exceptionHandlers(
        final ExceptionHandler<? extends Throwable>... exceptionHandlers) {
      for (final ExceptionHandler<? extends Throwable> exceptionHandler : exceptionHandlers) {
        this.exceptionHandlers.add((ExceptionHandler<Throwable>) exceptionHandler);
      }

      return this;
    }

    public CommandHandler build() {
      return new CommandHandler(this.prefix, this.ownerId, this.guildSettingsProvider,
          this.uncaughtExceptionHandler, this.replies, this.commands, this.contexts,
          this.exceptionHandlers);
    }
  }
}
