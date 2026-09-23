package com.supporttickets.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TicketStatusMachineTest {

  private record Edge(TicketStatus from, TicketStatus to) {
  }

  private static final Set<Edge> ALLOWED_EDGES = Set.of(
      new Edge(TicketStatus.OPEN, TicketStatus.IN_PROGRESS),
      new Edge(TicketStatus.OPEN, TicketStatus.CANCELLED),
      new Edge(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED),
      new Edge(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED),
      new Edge(TicketStatus.RESOLVED, TicketStatus.CLOSED)
  );

  static Stream<Arguments> allowedTransitions() {
    return ALLOWED_EDGES.stream().map(e -> Arguments.of(e.from(), e.to()));
  }

  static Stream<Arguments> allStatusPairs() {
    return Stream.of(TicketStatus.values())
        .flatMap(from -> Stream.of(TicketStatus.values()).map(to -> Arguments.of(from, to)));
  }

  @ParameterizedTest
  @MethodSource("allowedTransitions")
  void allowsDocumentedEdges(TicketStatus from, TicketStatus to) {
    assertThat(TicketStatusMachine.canTransition(from, to)).isTrue();
    TicketStatusMachine.assertAllowed(from, to);
  }

  @ParameterizedTest
  @MethodSource("allStatusPairs")
  void rejectsEveryTransitionNotExplicitlyAllowed(TicketStatus from, TicketStatus to) {
    boolean expected = ALLOWED_EDGES.contains(new Edge(from, to));
    assertThat(TicketStatusMachine.canTransition(from, to)).isEqualTo(expected);
    if (!expected) {
      assertThatThrownBy(() -> TicketStatusMachine.assertAllowed(from, to))
          .isInstanceOf(IllegalTransitionException.class)
          .hasMessageContaining(String.valueOf(from))
          .hasMessageContaining(String.valueOf(to));
    }
  }

  @Test
  void rejectsClosedToOpen() {
    assertThat(TicketStatusMachine.canTransition(TicketStatus.CLOSED, TicketStatus.OPEN)).isFalse();
  }

  @Test
  void rejectsResolvedToOpen() {
    assertThat(TicketStatusMachine.canTransition(TicketStatus.RESOLVED, TicketStatus.OPEN)).isFalse();
  }

  @Test
  void rejectsCancelledToOpen() {
    assertThat(TicketStatusMachine.canTransition(TicketStatus.CANCELLED, TicketStatus.OPEN)).isFalse();
  }

  @Test
  void rejectsSkippedSteps() {
    assertThat(TicketStatusMachine.canTransition(TicketStatus.OPEN, TicketStatus.RESOLVED)).isFalse();
    assertThat(TicketStatusMachine.canTransition(TicketStatus.OPEN, TicketStatus.CLOSED)).isFalse();
    assertThat(TicketStatusMachine.canTransition(TicketStatus.IN_PROGRESS, TicketStatus.CLOSED)).isFalse();
  }

  @Test
  void allowedTargetsMatchEdges() {
    assertThat(TicketStatusMachine.allowedTargets(TicketStatus.OPEN))
        .isEqualTo(EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED));
    assertThat(TicketStatusMachine.allowedTargets(TicketStatus.IN_PROGRESS))
        .isEqualTo(EnumSet.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED));
    assertThat(TicketStatusMachine.allowedTargets(TicketStatus.RESOLVED))
        .isEqualTo(EnumSet.of(TicketStatus.CLOSED));
    assertThat(TicketStatusMachine.allowedTargets(TicketStatus.CLOSED)).isEmpty();
    assertThat(TicketStatusMachine.allowedTargets(TicketStatus.CANCELLED)).isEmpty();
  }
}
