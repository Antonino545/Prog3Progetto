package it.unito.prog3progetto.Lib;

import java.util.Collections;
import java.util.List;

public class Mail {
  private final String sender;
  private final String destination;


  private final String subject;
  private final String content;

  public Mail(String sender, String destination, String subject, String content) {
    this.sender = sender;
    this.destination = destination;
    this.subject = subject;
    this.content = content;
  }

  public String getSender() {
    return sender;
  }
  public String getDestination() {
    return destination;
  }

  public String getSubject() {
    return subject;
  }

  public String getContent() {
    return content;
  }

  @Override
  public String toString() {
    return "Mail{" +
            "sender='" + sender + '\'' +
            ", destination='" + destination + '\'' +
            ", subject='" + subject + '\'' +
            ", content='" + content + '\'' +
            '}';

  }
}
