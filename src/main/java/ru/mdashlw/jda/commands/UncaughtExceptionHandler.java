package ru.mdashlw.jda.commands;

public interface UncaughtExceptionHandler extends ExceptionHandler<Throwable> {

  @Override
  default Class<Throwable> getType() {
    return Throwable.class;
  }
}
