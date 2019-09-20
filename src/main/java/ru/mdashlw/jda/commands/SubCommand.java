package ru.mdashlw.jda.commands;

import java.util.EnumSet;
import net.dv8tion.jda.api.Permission;

public abstract class SubCommand extends Command {

  private final Command parent;

  public SubCommand(final Command parent) {
    this.parent = parent;
  }

  @Override
  public Category getCategory() {
    return this.parent.getCategory();
  }

  @Override
  public String getQualifiedName() {
    return this.parent.getQualifiedName() + ' ' + this.getName();
  }

  @Override
  public EnumSet<Permission> getMemberPermissions() {
    return this.parent.getMemberPermissions();
  }

  @Override
  public EnumSet<Permission> getBotPermissions() {
    return this.parent.getBotPermissions();
  }

  @Override
  public boolean isShownInHelp() {
    return false;
  }

  @Override
  public boolean hasAccess(final Event event) {
    return this.parent.hasAccess(event);
  }

  public final Command getParent() {
    return this.parent;
  }
}
