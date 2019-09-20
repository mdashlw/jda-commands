package ru.mdashlw.jda.commands.replies;

import java.awt.Color;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import ru.mdashlw.jda.commands.Command;
import ru.mdashlw.jda.commands.Command.Event;
import ru.mdashlw.jda.commands.Replies;

// TODO Move to other package
public final class DefaultReplies implements Replies {

  public static final Color ERROR_COLOR = Color.RED; // TODO Find a better color
  public static final char CHECK_EMOJI = '\u2714';
  public static final char X_EMOJI = '\u274C';

  @Override
  public MessageAction replyHelp(final Command command, final Event event) {
    final String prefix = event.getGuildSettings().getPrefix();
    final String name = command.getName();
    final String description = command.getLongDescription();
    final String usage = command.getQualifiedUsage();
    final List<String> examples = command.getQualifiedExamples();
    final List<String> aliases = command.getAliases();
    final EnumSet<Permission> memberPermissions = command.getMemberPermissions();
    final EnumSet<Permission> botPermissions = command.getBotPermissions();
    final Member selfMember = event.getGuild().getSelfMember();
    final SelfUser selfUser = event.getJDA().getSelfUser();

    final EmbedBuilder builder = new EmbedBuilder()
        .setColor(Role.DEFAULT_COLOR_RAW) // TODO Find a new color
        .setTimestamp(Instant.now())
        .setTitle("Command " + name)
        .setDescription(description)
        .setFooter(selfMember.getEffectiveName(), selfUser.getEffectiveAvatarUrl())
        .addField("Usage:", "```\n" + prefix + usage + "\n```", false);

    if (!examples.isEmpty()) {
      builder.addField("Examples:", examples.stream()
          .map(prefix::concat)
          .collect(Collectors.joining("\n", "```\n", "\n```")), false);
    }

    if (!aliases.isEmpty()) {
      builder.addField("Aliases:", aliases.stream()
          .map(alias -> '`' + alias + '`')
          .collect(Collectors.joining(", ")), false);
    }

    if (!memberPermissions.isEmpty()) {
      builder.addField("Required User Permissions:", memberPermissions.stream()
          .map(Permission::getName)
          .collect(Collectors.joining("\n", "**", "**")), true);
    }

    if (!botPermissions.isEmpty()) {
      builder.addField("Required Bot Permissions:", botPermissions.stream()
          .map(Permission::getName)
          .collect(Collectors.joining("\n", "**", "**")), true);
    }

    return event.reply(builder.build());
  }

  @Override
  public MessageAction replyError(final Command command, final Event event, final String message) {
    // TODO Think about a better reply
    return event.reply(new EmbedBuilder()
        .setColor(ERROR_COLOR)
        .setDescription(message)
        .build());
  }

  @Override
  public MessageAction replyBlacklistedChannel(final Command command, final Event event) {
    return event.reply("Commands are disabled in this channel.");
  }

  @Override
  public MessageAction replyNoAccess(final Command command, final Event event) {
    return this.replyError(command, event, "You do not have access to use this command!");
  }

  // TODO Think about a better reply
  @Override
  public MessageAction replyNoMemberPermissions(final Command command, final Event event,
      final Collection<Permission> permissions) {
    final TextChannel channel = event.getChannel();
    final Member member = event.getMember();
    final String formattedPermissions = this.formatPermissions(channel, member, permissions);

    return event.reply(new EmbedBuilder()
        .setColor(ERROR_COLOR)
        .setDescription("You do not have permissions to use this command!")
        .addField("Required Permissions", formattedPermissions, false)
        .build());
  }

  // TODO Think about a better reply
  @Override
  public MessageAction replyNoBotPermissions(final Command command, final Event event,
      final Collection<Permission> permissions) {
    final TextChannel channel = event.getChannel();
    final Member member = event.getGuild().getSelfMember();
    final String formattedPermissions = this.formatPermissions(channel, member, permissions);

    return event.reply(new EmbedBuilder()
        .setColor(ERROR_COLOR)
        .setDescription("I do not have permissions to execute this command!")
        .addField("Required Permissions", formattedPermissions, false)
        .build());
  }

  // TODO Think about a better reply, e.g. provide instructions how to give such permission, etc
  @Override
  public MessageAction replyNoEmbedLinksPermission(final Command command, final Event event) {
    return event.reply("I need the **Embed Links** permission.");
  }

  private String formatPermissions(final TextChannel channel, final Member member,
      final Collection<Permission> permissions) {
    return permissions.stream()
        .map(permission -> {
          final char emoji = member.hasPermission(channel, permission) ? CHECK_EMOJI : X_EMOJI;

          return emoji + " **" + permission.getName() + "**";
        })
        .collect(Collectors.joining("\n"));
  }
}
