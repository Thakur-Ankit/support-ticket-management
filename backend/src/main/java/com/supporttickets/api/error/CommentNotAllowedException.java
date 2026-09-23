package com.supporttickets.api.error;

public class CommentNotAllowedException extends RuntimeException {

  public CommentNotAllowedException(String message) {
    super(message);
  }
}
