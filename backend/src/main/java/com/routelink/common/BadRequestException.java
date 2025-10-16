package com.routelink.common;

public class BadRequestException extends RuntimeException {
  public BadRequestException(String msg) { super(msg); }
}
