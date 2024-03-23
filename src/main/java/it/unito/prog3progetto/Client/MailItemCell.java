package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Lib.Email;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

class MailItemCell extends ListCell<Email> {
  private final Stage primaryStage;

  public MailItemCell(Stage primaryStage) {
    this.primaryStage = primaryStage;
  }

  @Override
  protected void updateItem(Email email, boolean empty) {
    super.updateItem(email, empty);
    if (email != null && !empty) {
      VBox vbox = new VBox();
      Label senderLabel = new Label(email.getSender());
      Label subjectLabel = new Label(email.getSubject());
      Label contentLabel = new Label(email.getContent().split("\\n")[0]); // Prende solo la prima riga del contenuto

      vbox.getChildren().addAll(senderLabel, subjectLabel, contentLabel);
      setGraphic(vbox);
      getStyleClass().add("emailitem");
      setOnMouseClicked(event -> {
        try {
          FXMLLoader loader = new FXMLLoader(getClass().getResource("MailDetailView.fxml"));
          Parent root = loader.load();

          MailDetailController controller = loader.getController();
          controller.setMailDetails(email.getSender(), email.getSubject(), email.getContent());

          Stage stage = new Stage();
          stage.setScene(new Scene(root));
          stage.setTitle("Dettagli Email");
          stage.setResizable(false);
          stage.show();
        } catch (IOException e) {
    System.out.println("Errore durante l'apertura della finestra di dettaglio email");
        }
      });
    } else {
      setText(null);
      setGraphic(null);
    }
  }
}
