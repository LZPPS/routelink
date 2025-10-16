// src/main/java/com/routelink/common/MaskingUtil.java
package com.routelink.common;

public final class MaskingUtil {
  private MaskingUtil() {}

  public static String maskEmail(String email) {
    if (email == null || !email.contains("@")) return "****";
    String[] parts = email.split("@", 2);
    String name = parts[0];
    String domain = parts[1];
    String visible = name.length() <= 2 ? name.substring(0, 1) : name.substring(0, 2);
    return visible + "****@" + domain;
  }

  public static String maskPhone(String phone) {
    if (phone == null || phone.length() < 4) return "********";
    String last4 = phone.substring(phone.length() - 4);
    return "*******" + last4;
  }
}
