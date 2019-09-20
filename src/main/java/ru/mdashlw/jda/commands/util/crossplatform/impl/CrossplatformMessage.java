package ru.mdashlw.jda.commands.util.crossplatform.impl;

import java.util.function.Supplier;
import net.dv8tion.jda.api.entities.Message;
import ru.mdashlw.jda.commands.util.crossplatform.CrossplatformEntity;

public final class CrossplatformMessage extends CrossplatformEntity<Message> {

  public CrossplatformMessage(final Supplier<? extends Message> desktop,
      final Supplier<? extends Message> mobile) {
    super(desktop, mobile);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private Supplier<? extends Message> desktop;
    private Supplier<? extends Message> mobile;

    public Builder desktop(final Supplier<? extends Message> desktop) {
      this.desktop = desktop;
      return this;
    }

    public Builder mobile(final Supplier<? extends Message> mobile) {
      this.mobile = mobile;
      return this;
    }

    public CrossplatformMessage build() {
      return new CrossplatformMessage(this.desktop, this.mobile);
    }
  }
}
