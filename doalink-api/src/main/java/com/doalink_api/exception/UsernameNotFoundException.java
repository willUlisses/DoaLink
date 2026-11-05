package com.doalink_api.exception;

public class UsernameNotFound extends RuntimeException {
  public UsernameNotFound(String message) {
    super(message);
  }
}
