package com.routelink.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
	  private final JavaMailSender mailSender;

	  @Value("${app.mail.from:RouteLink <no-reply@routelink.local>}")
	  private String from;

	  public EmailService(JavaMailSender mailSender) { this.mailSender = mailSender; }

	  public void sendText(String to, String subject, String body) {
	    SimpleMailMessage m = new SimpleMailMessage();
	    m.setFrom(from);
	    m.setTo(to);
	    m.setSubject(subject);
	    m.setText(body);
	    mailSender.send(m);
	  }

	  public void sendTextWithReplyTo(String to, String subject, String body, String replyTo) {
	    SimpleMailMessage m = new SimpleMailMessage();
	    m.setFrom(from);
	    m.setTo(to);
	    m.setSubject(subject);
	    m.setText(body);
	    if (replyTo != null && !replyTo.isBlank()) m.setReplyTo(replyTo); // replies go to driver/rider
	    mailSender.send(m);
	  }
	}
