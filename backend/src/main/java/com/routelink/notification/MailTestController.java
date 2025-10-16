package com.routelink.notification;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MailTestController {
  private final EmailService email;
  public MailTestController(EmailService email) { this.email = email; }

  @GetMapping("/dev/mail/test")   // <-- EXACT path you’re hitting
  public String test(@RequestParam String to) {
    email.sendText(
      to,
      "RouteLink Test",
      "✅ If you see this, Gmail SMTP is working.\n\n– RouteLink"
    );
    return "Sent to " + to;
  }
}
