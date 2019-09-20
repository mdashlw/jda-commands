package ru.mdashlw.jda.commands.contexts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;
import ru.mdashlw.jda.commands.Command.Event;
import ru.mdashlw.jda.commands.Context;

public final class LongContext implements Context<Long> {

  @Override
  public Class<Long> getType() {
    return Long.TYPE;
  }

  @Override
  public Long resolve(final Event event, final int index, final Parameter parameter,
      final String text, final String arg) {
    final long number;

    try {
      number = Long.parseLong(arg);
    } catch (final NumberFormatException exception) {
      event.replyError(String.format("Input `%s` should be a number.", arg)).queue();
      return null;
    }

    if (parameter.isAnnotationPresent(From.class)) {
      final long from = parameter.getAnnotation(From.class).value();

      if (number < from) {
        event.replyError(
            String.format("Number `%,d` should be greater than or equal to **%,d**.", number, from))
            .queue();
        return null;
      }
    }

    if (parameter.isAnnotationPresent(To.class)) {
      final long to = parameter.getAnnotation(To.class).value();

      if (number > to) {
        event.replyError(
            String.format("Number `%,d` should be less than or equal to **%,d**.", number, to))
            .queue();
        return null;
      }
    }

    return number;
  }

  @Target(ElementType.PARAMETER)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface From {

    long value();
  }

  @Target(ElementType.PARAMETER)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface To {

    long value();
  }
}
