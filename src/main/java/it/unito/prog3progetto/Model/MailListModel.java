package it.unito.prog3progetto.Model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

public class MailListModel {
  private final ObservableList<Email> emails = FXCollections.observableArrayList();
  private final ArrayList<MailListObserver> observers = new ArrayList<>();
  private final IntegerProperty sizeProperty = new SimpleIntegerProperty();

  public void addObserver(MailListObserver observer) {
    observers.add(observer);
  }

  public void removeObserver(MailListObserver observer) {
    observers.remove(observer);
  }

  public void addEmail(Email email) {
    emails.add(email);
    sizeProperty.set(emails.size());
    notifyEmailAdded(email);
  }

  public void removeEmail(Email email) {
    emails.remove(email);
    sizeProperty.set(emails.size());
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

  public void clear() {
    emails.clear();
    sizeProperty.set(0);
    for (MailListObserver observer : observers) {
      observer.onAllEmailsRemoved();
    }
  }

  public ObservableList<Email> getEmails() {
    return emails;
  }

  public void addEmails(ArrayList<Email> emails) {
    this.emails.addAll(emails);
    sizeProperty.set(this.emails.size());
    for (Email email : emails) {
      notifyEmailAdded(email);
    }
  }

  public int size() {
    return emails.size();
  }

  public IntegerProperty sizeProperty() {
    return sizeProperty;
  }
}