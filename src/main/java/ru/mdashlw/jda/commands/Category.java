package ru.mdashlw.jda.commands;

import java.util.EnumSet;
import net.dv8tion.jda.api.Permission;

public interface Category {

  String getName();

  EnumSet<Permission> getMemberPermissions();

  EnumSet<Permission> getBotPermissions();

  boolean isShownInHelp();

  boolean hasAccess(Command.Event event);
}
