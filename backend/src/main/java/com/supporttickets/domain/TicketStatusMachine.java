package com.supporttickets.domain;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Pure domain state machine for ticket status. Allowed edges only:
 * OPEN→IN_PROGRESS, IN_PROGRESS→RESOLVED, RESOLVED→CLOSED,
 * OPEN→CANCELLED, IN_PROGRESS→CANCELLED. All other transitions rejected.
 */
public final class TicketStatusMachine {

  private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED = new EnumMap<>(TicketStatus.class);

  static {
    ALLOWED.put(TicketStatus.OPEN, EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED));
    ALLOWED.put(TicketStatus.IN_PROGRESS, EnumSet.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED));
    ALLOWED.put(TicketStatus.RESOLVED, EnumSet.of(TicketStatus.CLOSED));
    ALLOWED.put(TicketStatus.CLOSED, EnumSet.noneOf(TicketStatus.class));
    ALLOWED.put(TicketStatus.CANCELLED, EnumSet.noneOf(TicketStatus.class));
  }

  private TicketStatusMachine() {
  }

  public static boolean canTransition(TicketStatus from, TicketStatus to) {
    if (from == null || to == null || from == to) {
      return false;
    }
    Set<TicketStatus> next = ALLOWED.get(from);
    return next != null && next.contains(to);
  }

  public static void assertAllowed(TicketStatus from, TicketStatus to) {
    if (!canTransition(from, to)) {
      throw new IllegalTransitionException(
          "Illegal status transition from " + from + " to " + to);
    }
  }

  public static Set<TicketStatus> allowedTargets(TicketStatus from) {
    if (from == null) {
      return EnumSet.noneOf(TicketStatus.class);
    }
    Set<TicketStatus> next = ALLOWED.get(from);
    return next == null ? EnumSet.noneOf(TicketStatus.class) : EnumSet.copyOf(next);
  }
}
