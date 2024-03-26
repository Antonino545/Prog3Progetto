package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Lib.Email;

public interface MailListObserver {
  void onEmailAdded(Email email);
  void onEmailRemoved(Email email);

  void onAllEmailsRemoved();

  // Implementazione del metodo dell'interfaccia MailListObserver per gestire l'aggiunta di email
}
