package it.unito.prog3progetto.Model;

public interface MailListObserver {
  void onEmailAdded(Email email);
  void onEmailRemoved(Email email);

  void onAllEmailsRemoved();
}
