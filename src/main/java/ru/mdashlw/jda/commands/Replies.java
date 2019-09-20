package ru.mdashlw.jda.commands;

import java.util.Collection;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public interface Replies {

  MessageAction replyHelp(Command command, Command.Event event);

  MessageAction replyError(Command command, Command.Event event, String message);

  MessageAction replyBlacklistedChannel(Command command, Command.Event event);

  MessageAction replyNoAccess(Command command, Command.Event event);

  MessageAction replyNoMemberPermissions(Command command, Command.Event event,
      Collection<Permission> permissions);

  MessageAction replyNoBotPermissions(Command command, Command.Event event,
      Collection<Permission> permissions);

  MessageAction replyNoEmbedLinksPermission(Command command, Command.Event event);
}
