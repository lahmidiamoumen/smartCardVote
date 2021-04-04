package numidia.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import numidia.Main;
import numidia.reader.CitizenCardEventListener;
import numidia.reader.CitizenCardReader;
import numidia.reader.model.CardData;
import numidia.reader.model.ReadingStatus;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Home implements Initializable {

    // private CitizenCardReader citizenCard;


    @FXML
    Button close,minimise,maximise;
    public static BorderPane searchPane;
    @FXML
    public BorderPane holdPane;


    @Override
    public void initialize(URL location, ResourceBundle resources) {


        try {
            searchPane = FXMLLoader.load(getClass().getResource("../view/search.fxml"));
            setNode(searchPane);
        } catch (IOException ex) {
            Logger.getLogger(Home.class.getName()).log(Level.SEVERE, null, ex);
        }
        eventSearchPane();
        close.setOnAction(event -> System.exit(0));
        minimise.setOnAction(e -> Main.primaryStage.setIconified(true));
        maximise.setOnAction(e -> Main.primaryStage.setMaximized(!Main.primaryStage.isMaximized()));
    }

    public void setNode(BorderPane node1) {
        BorderPane node = new BorderPane(node1);
        holdPane.getChildren().clear();
        holdPane.setTop(node.getTop());
        holdPane.setBottom(node.getBottom());
        holdPane.setLeft(node.getLeft());
        holdPane.setRight(node.getRight());
        holdPane.setCenter(node.getCenter());
    }

    void eventSearchPane(){
        HBox hBox= (HBox) searchPane.getCenter();
        AnchorPane anchorPane = (AnchorPane) hBox.getChildren().get(0);
        Button btn = (Button) anchorPane.getChildren().get(1);
        TextField input = (TextField) anchorPane.getChildren().get(0);
        input.setOnKeyPressed(ke -> { if (ke.getCode().equals(KeyCode.ENTER) && !input.getText().isEmpty() && !input.getText().matches("[ ]+")) {
            System.out.println(input.getText()); } });
        btn.setOnAction(e ->{
            if(!input.getText().isEmpty() && !input.getText().matches("[ ]+"))
                System.out.println(input.getText());
        });
    }

}
