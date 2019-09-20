package ru.mdashlw.jda.commands.util.crossplatform;

import java.util.function.Supplier;
import net.dv8tion.jda.api.entities.Member;
import ru.mdashlw.jda.commands.util.MemberUtils;

public abstract class CrossplatformEntity<T> {

  private final Supplier<? extends T> desktop;
  private final Supplier<? extends T> mobile;

  public CrossplatformEntity(final Supplier<? extends T> desktop,
      final Supplier<? extends T> mobile) {
    this.desktop = desktop;
    this.mobile = mobile;
  }

  public final T get(final Member member) {
    if (MemberUtils.isOnlineFromDesktop(member)) {
      return this.getDesktop();
    }

    return this.getMobile();
  }

  public final T getDesktop() {
    return this.desktop.get();
  }

  public final T getMobile() {
    return this.mobile.get();
  }
}
