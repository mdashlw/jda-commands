package ru.mdashlw.jda.commands.util.waiter;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public final class EventWaiter implements EventListener {

  private final Map<Class<? extends GenericEvent>, Set<WaitingEvent<? super GenericEvent>>> events = new HashMap<>();
  private final ScheduledExecutorService executor;

  public EventWaiter() {
    this(Executors.newSingleThreadScheduledExecutor());
  }

  public EventWaiter(final ScheduledExecutorService executor) {
    this.executor = executor;
  }

  @Override
  public void onEvent(@Nonnull final GenericEvent event) {
    final Set<WaitingEvent<? super GenericEvent>> waitingEvents = this.events.get(event.getClass());

    waitingEvents.stream()
        .filter(waitingEvent -> waitingEvent.canExecute(event))
        .forEach(waitingEvent -> {
          waitingEvent.execute(event);
          waitingEvents.remove(waitingEvent);
        });
  }

  public <T extends GenericEvent> void waitForEvent(final Class<T> target,
      final Predicate<? super T> predicate, final Consumer<? super T> action) {
    this.waitForEvent(target, Duration.ZERO, predicate, action);
  }

  public <T extends GenericEvent> void waitForEvent(final Class<T> target, final Duration timeout,
      final Predicate<? super T> predicate, final Consumer<? super T> action) {
    this.waitForEvent(target, timeout, null, predicate, action);
  }

  public <T extends GenericEvent> void waitForEvent(final Class<T> target, final Duration timeout,
      final Runnable timeoutAction, final Predicate<? super T> predicate,
      final Consumer<? super T> action) {
    this.register(new WaitingEvent<>(target, timeout, timeoutAction, predicate, action));
  }

  @SuppressWarnings("unchecked")
  public void register(final WaitingEvent<? extends GenericEvent> event) {
    final Set<WaitingEvent<? super GenericEvent>> waitingEvents = this.events
        .computeIfAbsent(event.getTarget(), ignored -> new HashSet<>());

    waitingEvents.add((WaitingEvent<? super GenericEvent>) event);

    final Duration timeout = event.getTimeout();
    final Runnable timeoutAction = event.getTimeoutAction();

    if (timeout != null && !timeout.isZero()) {
      this.executor.schedule(() -> {
        if (waitingEvents.remove(event) && timeoutAction != null) {
          timeoutAction.run();
        }
      }, timeout.getSeconds(), TimeUnit.SECONDS);
    }
  }
}
