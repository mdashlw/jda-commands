package ru.mdashlw.jda.commands.util.waiter;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.dv8tion.jda.api.events.GenericEvent;

public final class WaitingEvent<T extends GenericEvent> {

  private final Class<T> target;
  private final Duration timeout;
  private final Runnable timeoutAction;
  private final Predicate<? super T> predicate;
  private final Consumer<? super T> action;

  public WaitingEvent(final Class<T> target, final Duration timeout,
      final Runnable timeoutAction,
      final Predicate<? super T> predicate, final Consumer<? super T> action) {
    this.target = target;
    this.timeout = timeout;
    this.timeoutAction = timeoutAction;
    this.predicate = predicate;
    this.action = action;
  }

  public boolean canExecute(final T event) {
    return this.predicate.test(event);
  }

  public void execute(final T event) {
    this.action.accept(event);
  }

  public Class<T> getTarget() {
    return this.target;
  }

  public Duration getTimeout() {
    return this.timeout;
  }

  public Runnable getTimeoutAction() {
    return this.timeoutAction;
  }

  public Predicate<? super T> getPredicate() {
    return this.predicate;
  }

  public Consumer<? super T> getAction() {
    return this.action;
  }
}
