package ru.mdashlw.jda.commands.contexts;

import java.lang.reflect.Parameter;
import ru.mdashlw.jda.commands.Command.Event;
import ru.mdashlw.jda.commands.Context;

public final class BooleanContext implements Context<Boolean> {

  @Override
  public Class<Boolean> getType() {
    return Boolean.TYPE;
  }

  @Override
  public Boolean resolve(final Event event, final int index, final Parameter parameter,
      final String text, final String arg) {
    try {
      return this.parseBoolean(arg);
    } catch (final IllegalArgumentException exception) {
      event.replyError(String.format("Input `%s` should be a logical value. **(yes/no)**", arg))
          .queue();
      return null;
    }
  }

  public boolean parseBoolean(final String s) {
    if ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s) || "1".equals(s)) {
      return true;
    }

    if ("false".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s) || "0".equals(s)) {
      return false;
    }

    throw new IllegalArgumentException("value is not a boolean");
  }
}
