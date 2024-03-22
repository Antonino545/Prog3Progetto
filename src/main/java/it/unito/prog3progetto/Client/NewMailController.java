package it.unito.prog3progetto.Client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewMailController {
  @FXML
  public TextField subjectfield;
  @FXML
  public TextField destinationsfield;
  @FXML
  public TextArea ContentField;


  public void sendMail(ActionEvent actionEvent) {
    System.out.println("Invio email");
    subjectfield.getText();
    ContentField.getText();

    String destination = destinationsfield.getText();
    String[] destinationsArray = destination.split(",");
    List<String> destinationsList = Arrays.asList(destinationsArray);
    System.out.println("Destinatari: " + destinationsList);
    System.out.println("Oggetto: " + subjectfield.getText());
    System.out.println("Contenuto: " + ContentField.getText());
    Stage stage = (Stage) subjectfield.getScene().getWindow();
    stage.close();
  }

}