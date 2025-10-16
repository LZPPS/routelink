package com.routelink.booking;

public enum BookingStatus {
  REQUESTED,   // created by rider
  CONFIRMED,   // accepted by driver
  DECLINED,    // rejected by driver
  CANCELLED    // (optional) cancelled by rider/driver
}
