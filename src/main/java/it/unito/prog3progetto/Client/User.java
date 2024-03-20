package it.unito.prog3progetto.Client;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {

@Serial
private static final long serialVersionUID = 5950169519310163575L;
	private String email;
private String password;


public User(String email, String scrittura) {
	this.email = email;
	this.password = scrittura;
}

	public String getEmail() {
	return email;
}

public String getPassword() {
	return password;
}

public void setPassword(String password) {
	this.password = password;
}

public boolean equals(Object o) {
	if (this == o)
		return true;
	if (o == null || getClass() != o.getClass())
		return false;

	User user = (User) o;

  return Objects.equals(email, user.email);
}



public String toString() {
	return "  Email = " + getEmail() + " ; Passwors = " + getPassword();
}
}