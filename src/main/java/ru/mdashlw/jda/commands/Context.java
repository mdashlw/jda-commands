package ru.mdashlw.jda.commands;

import java.lang.reflect.Parameter;

public interface Context<T> {

  Class<T> getType();

  T resolve(Command.Event event, int index, Parameter parameter, String text, String arg);
}
