package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Lib.Email;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.SequencedCollection;

public class MailListModel {
  private final ObservableList<Email> emails = FXCollections.observableArrayList();
  private final ArrayList<MailListObserver> observers = new ArrayList<>();

  public void addObserver(MailListObserver observer) {
    observers.add(observer);
  }

  public void removeObserver(MailListObserver observer) {
    observers.remove(observer);
  }



  public void addEmail(Email email) {
    emails.add(email);
    notifyEmailAdded(email);
  }

  public void removeEmail(Email email) {
    emails.remove(email);
    notifyEmailRemoved(email);
  }

  private void notifyEmailAdded(Email email) {
    for (MailListObserver observer : observers) {
      observer.onEmailAdded(email);
    }
  }

  private void notifyEmailRemoved(Email email) {
    for (MailListObserver observer : observers) {
      observer.onEmailRemoved(email);
    }
  }

  public ObservableList<Email> getEmails() {
    return emails;
  }

  public void addEmails(ArrayList<Email> emails) {
    this.emails.addAll(emails);
    for (Email email : emails) {
      notifyEmailAdded(email);
    }
  }
}
