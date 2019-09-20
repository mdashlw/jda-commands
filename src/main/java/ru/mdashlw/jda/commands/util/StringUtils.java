package ru.mdashlw.jda.commands.util;

import java.util.regex.Pattern;

public final class StringUtils {

  private static final Pattern SPACES = Pattern.compile("\\s+");

  private StringUtils() {
  }

  public static String stripExtraSpaces(final String s) {
    return SPACES.matcher(s).replaceAll(" ");
  }
}
