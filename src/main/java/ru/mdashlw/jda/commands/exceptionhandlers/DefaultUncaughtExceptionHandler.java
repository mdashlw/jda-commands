package ru.mdashlw.jda.commands.exceptionhandlers;

import java.time.Instant;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import ru.mdashlw.jda.commands.Command;
import ru.mdashlw.jda.commands.Command.Event;
import ru.mdashlw.jda.commands.UncaughtExceptionHandler;
import ru.mdashlw.jda.commands.replies.DefaultReplies;

// TODO Move to other package
public final class DefaultUncaughtExceptionHandler implements UncaughtExceptionHandler {

  @Override
  public void handle(final Command command, final Event event, final Throwable exception) {
    // TODO Use proper logging
    exception.printStackTrace();

    final long ownerId = event.getHandler().getOwnerId();
    final User owner = ownerId != 0L ? event.getJDA().getUserById(ownerId) : null;

    event.reply(new EmbedBuilder()
        .setColor(DefaultReplies.ERROR_COLOR)
        .setTitle("Critical Error")
        .appendDescription("**Type:** `")
        .appendDescription(exception.getClass().getName())
        .appendDescription("`\n**Message:** `")
        .appendDescription(exception.getMessage())
        .appendDescription("`")
        .setFooter(
            owner != null ? String.format("Contact %#s", owner) : "Contact the bot developer",
            owner != null ? owner.getEffectiveAvatarUrl() : null)
        .setTimestamp(Instant.now())
        .build()).queue();
  }
}
