package ru.mdashlw.jda.commands.contexts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;
import ru.mdashlw.jda.commands.Command.Event;
import ru.mdashlw.jda.commands.Context;

public final class StringContext implements Context<String> {

  @Override
  public Class<String> getType() {
    return String.class;
  }

  @Override
  public String resolve(final Event event, final int index, final Parameter parameter,
      final String text, final String arg) {
    return parameter.isAnnotationPresent(Text.class) ? text : arg;
  }

  @Target(ElementType.PARAMETER)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Text {

  }
}
