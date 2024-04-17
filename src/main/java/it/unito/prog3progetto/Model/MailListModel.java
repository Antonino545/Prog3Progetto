package it.unito.prog3progetto.Model;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

public class MailListModel {
  private final ObservableList<Email> emails = FXCollections.observableArrayList();
  private final ArrayList<MailListObserver> observers = new ArrayList<>();
  private final IntegerProperty sizeProperty = new SimpleIntegerProperty();

  /**
   * Aggiunge un osservatore alla lista
   * @param observer Osservatore da aggiungere
   */
  public void addObserver(MailListObserver observer) {
    observers.add(observer);
  }

  public void removeObserver(MailListObserver observer) {
    observers.remove(observer);
  }

  /**
   * Aggiunge un'email alla lista
   * @param email Email da aggiungere
   */
  public void addEmail(Email email) {
    if(emails.contains(email)) return; // Se l'email è già presente nella lista, non la aggiungiamo (non dovrebbe mai accadere
    emails.add(email);
    sizeProperty.set(emails.size());
    notifyEmailAdded(email);
  }

  /**
   * Rimuove un'email dalla lista
   * @param email Email da rimuovere
   */
  public void removeEmail(Email email) {
    emails.remove(email);
    sizeProperty.set(emails.size());
    notifyEmailRemoved(email);// Notifica l'osservatore
  }

  /**
   * Notifica l'aggiunta di un'email
   * @param email Email aggiunta
   */
  private void notifyEmailAdded(Email email) {
    for (MailListObserver observer : observers) {
      observer.onEmailAdded(email);
    }
  }

  /**
   * Notifica la rimozione di un'email
   * @param email Email rimossa
   */
  private void notifyEmailRemoved(Email email) {
    for (MailListObserver observer : observers) {
      observer.onEmailRemoved(email);
    }
  }

  /**
   * Rimuove tutte le email dalla lista
   */
  public void clear() {
    emails.clear();
    sizeProperty.set(0);
    for (MailListObserver observer : observers) {
      observer.onAllEmailsRemoved();
    }
  }

  /**
   * Restituisce la lista di email
   * @return  Lista di email
   */
  public ObservableList<Email> getEmails() {
    return emails;
  }

  /**
   * Aggiunge una lista di email alla lista
   * @param emails Lista di email da aggiungere
   */
  public void addEmails(ArrayList<Email> emails) {
    Platform.runLater(() -> {
    this.emails.addAll(emails);
      sizeProperty.set(this.emails.size());
    });
    for (Email email : emails) {
      notifyEmailAdded(email);
    }
  }

  /**
   * Restituisce il numero di email presenti nella lista
   * @return Numero di email presenti nella lista
   */
  public int size() {
    return emails.size();
  }

  /**
   * Restituisce la proprietà che rappresenta il numero di email presenti nella lista
   * @return Proprietà che rappresenta il numero di email presenti nella lista
   */
  public IntegerProperty sizeProperty() {
    return sizeProperty;
  }
}