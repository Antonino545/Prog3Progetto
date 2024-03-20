package it.unito.prog3progetto.Client;

import java.util.Collections;
import java.util.List;

public class Mail {
  private final String sender;
  private final String subject;
  private final String content;

  public Mail(String sender, String subject, String content) {
    this.sender = sender;
    this.subject = subject;
    this.content = content;
  }

  public String getSender() {
    return sender;
  }

  public String getSubject() {
    return subject;
  }

  public String getContent() {
    return content;
  }

  @Override
  public String toString() {
    return subject;
  }
}