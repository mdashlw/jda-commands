package ru.mdashlw.jda.commands;

public interface ExceptionHandler<T extends Throwable> {

  Class<T> getType();

  void handle(Command command, Command.Event event, T exception);
}
