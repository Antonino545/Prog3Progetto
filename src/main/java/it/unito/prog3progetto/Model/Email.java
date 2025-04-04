package it.unito.prog3progetto.Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

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

  /**
   * Return the content of the email without the end line character (\n) and replace it with <--Accapo-->
   * @return Email
   */
  public Email emailNoEndLine(){
    return new Email(sender,destinations,subject,content.replace("\n","<--Accapo-->"),datesendMail,id);
  }



  public String getContent() {
    return content;
  }

  public Date getDatesendMail() {
    return datesendMail;
  }

  public StringProperty  getsenderProprierty(){
    return new SimpleStringProperty(getSender());
  }
  public StringProperty  getDestinationsProprierty(){
    return new SimpleStringProperty(getDestinations().toString());
  }
  public StringProperty  getSubjectProprierty(){
    return new SimpleStringProperty(getSubject());
  }
  public StringProperty getDatesendMailProprierty(){
    return new SimpleStringProperty(getItalianDate());
  }
  public StringProperty  getContentProprierty() {
    String content = getContent();
    if (content.length() > 50) {
      String truncatedContent = content.substring(0, 50);
      if (truncatedContent.trim().length() < content.trim().length()) {
        content = truncatedContent + "...";
      }
    }
    return new SimpleStringProperty(content);
  }

  @Override
  public String toString() {
    return  sender+" , "+destinations+" , "+subject+" , "+content+" , "+datesendMail+" , "+id ;
  }


}
