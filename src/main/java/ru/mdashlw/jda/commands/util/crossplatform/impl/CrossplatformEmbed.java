package ru.mdashlw.jda.commands.util.crossplatform.impl;

import java.util.function.Supplier;
import net.dv8tion.jda.api.entities.MessageEmbed;
import ru.mdashlw.jda.commands.util.crossplatform.CrossplatformEntity;

public final class CrossplatformEmbed extends CrossplatformEntity<MessageEmbed> {

  public CrossplatformEmbed(final Supplier<? extends MessageEmbed> desktop,
      final Supplier<? extends MessageEmbed> mobile) {
    super(desktop, mobile);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private Supplier<? extends MessageEmbed> desktop;
    private Supplier<? extends MessageEmbed> mobile;

    public Builder desktop(final Supplier<? extends MessageEmbed> desktop) {
      this.desktop = desktop;
      return this;
    }

    public Builder mobile(final Supplier<? extends MessageEmbed> mobile) {
      this.mobile = mobile;
      return this;
    }

    public CrossplatformEmbed build() {
      return new CrossplatformEmbed(this.desktop, this.mobile);
    }
  }
}
