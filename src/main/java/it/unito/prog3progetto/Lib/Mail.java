package it.unito.prog3progetto.Lib;

import java.util.ArrayList;
import java.util.Date;

public class Mail {
  private final String sender;
  private final ArrayList<String> destinations;

  private Date datesendMail;

  private final String subject;
  private final String content;



  public Mail(String sender, ArrayList<String> destinations, String subject, String content, Date datesendMail) {
    this.sender = sender;
    this.destinations = new ArrayList<>(destinations);
    this.subject = subject;
    this.content = content;
    this.datesendMail = datesendMail;
  }

  public String getSender() {
    return sender;
  }
  public ArrayList<String> getDestinations() {
    return destinations;
  }

  public String getSubject() {
    return subject;
  }

  public String getContent() {
    return content;
  }

  public Date getDatesendMail() {
    return datesendMail;
  }

  @Override
  public String toString() {
    return "Mail{" +
            "sender='" + sender + '\'' +
            ", destination='" + destinations + '\'' +
            ", subject='" + subject + '\'' +
            ", content='" + content + '\'' +
            ", datesendMail=" + datesendMail +
            '}';

  }
}
