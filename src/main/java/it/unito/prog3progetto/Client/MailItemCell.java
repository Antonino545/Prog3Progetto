package it.unito.prog3progetto.Client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

class MailItemCell extends ListCell<Mail> {
  private final Stage primaryStage;

  public MailItemCell(Stage primaryStage) {
    this.primaryStage = primaryStage;
  }

  @Override
  protected void updateItem(Mail mail, boolean empty) {
    super.updateItem(mail, empty);
    if (mail != null && !empty) {
      VBox vbox = new VBox();
      Label senderLabel = new Label(mail.getSender());
      Label subjectLabel = new Label(mail.getSubject());
      Label contentLabel = new Label(mail.getContent().split("\\n")[0]); // Prende solo la prima riga del contenuto

      vbox.getChildren().addAll(senderLabel, subjectLabel, contentLabel);
      setGraphic(vbox);
      getStyleClass().add("emailitem");
      setOnMouseClicked(event -> {
        try {
          FXMLLoader loader = new FXMLLoader(getClass().getResource("MailDetailView.fxml"));
          Parent root = loader.load();

          MailDetailController controller = loader.getController();
          controller.setMailDetails(mail.getSender(), mail.getSubject(), mail.getContent());

          Stage stage = new Stage();
          stage.setScene(new Scene(root));
          stage.setTitle("Dettagli Email");
          stage.setResizable(false);
          stage.show();
        } catch (IOException e) {
          e.printStackTrace();
        }
      });



    } else {
      setText(null);
      setGraphic(null);
    }
  }
}
