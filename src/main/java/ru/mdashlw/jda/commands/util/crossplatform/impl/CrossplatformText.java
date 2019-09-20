package ru.mdashlw.jda.commands.util.crossplatform.impl;

import java.util.function.Supplier;
import ru.mdashlw.jda.commands.util.crossplatform.CrossplatformEntity;

public final class CrossplatformText extends CrossplatformEntity<String> {

  public CrossplatformText(final Supplier<String> desktop, final Supplier<String> mobile) {
    super(desktop, mobile);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private Supplier<String> desktop;
    private Supplier<String> mobile;

    public Builder desktop(final Supplier<String> desktop) {
      this.desktop = desktop;
      return this;
    }

    public Builder mobile(final Supplier<String> mobile) {
      this.mobile = mobile;
      return this;
    }

    public CrossplatformText build() {
      return new CrossplatformText(this.desktop, this.mobile);
    }
  }
}
