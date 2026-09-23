package com.supporttickets.domain;

/**
 * Thrown when a ticket status transition is not an allowed edge.
 */
public class IllegalTransitionException extends RuntimeException {

  public IllegalTransitionException(String message) {
    super(message);
  }
}
