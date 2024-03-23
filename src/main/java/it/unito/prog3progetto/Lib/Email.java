package it.unito.prog3progetto.Lib;

import java.io.Serial;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Email implements Serializable {
  @Serial
  private static final long serialVersionUID = 5950169519310163575L;
  private final String sender;
  private final ArrayList<String> destinations;

  private final Date datesendMail;

  private final String subject;
  private final String content;

  private final  UUID id ;


  public Email(String sender, ArrayList<String> destinations, String subject, String content, Date datesendMail) {
    this.id = UUID.randomUUID();
    this.sender = sender;
    this.destinations = new ArrayList<>(destinations);
    this.subject = subject;
    this.content = content;
    this.datesendMail = datesendMail;
  }
  public Email(String sender, ArrayList<String> destinations, String subject, String content, Date datesendMail,UUID id) {
    this.id = id;
    this.sender = sender;
    this.destinations = new ArrayList<>(destinations);
    this.subject = subject;
    this.content = content;
    this.datesendMail = datesendMail;
  }
  public UUID getId() {
    return id;
  }
  public String getItalianDate() {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN);
    return sdf.format(datesendMail);
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
    return  sender+" , "+destinations+" , "+subject+" , "+content+" , "+datesendMail+" , "+id +"\n";
  }

}
