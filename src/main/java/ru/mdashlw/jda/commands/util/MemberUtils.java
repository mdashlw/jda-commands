package ru.mdashlw.jda.commands.util;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.ClientType;
import net.dv8tion.jda.api.entities.Member;

public final class MemberUtils {

  private MemberUtils() {
  }

  public static boolean isOnline(final Member member, final ClientType type) {
    final OnlineStatus status = member.getOnlineStatus(type);

    return status == OnlineStatus.ONLINE || status == OnlineStatus.DO_NOT_DISTURB;
  }

  public static boolean isOnlineFromDesktop(final Member member) {
    return isOnline(member, ClientType.DESKTOP) || isOnline(member, ClientType.WEB);
  }
}
