package numidia;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import numidia.reader.CitizenCardEventListener;
import numidia.reader.CitizenCardReader;
import numidia.reader.model.CardData;
import numidia.reader.model.ReadingStatus;

import javax.security.auth.callback.Callback;

public class Main extends Application implements CitizenCardEventListener {

    private double xOffset = 0;
    private double yOffset = 0;
    public static Stage primaryStage;
    private CitizenCardReader citizenCard;


    static {
        CitizenCardReader.loadLibrary();
    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        citizenCard = new CitizenCardReader();
        citizenCard.init();
        citizenCard.addListener(this);
        Parent root = FXMLLoader.load(getClass().getResource("view/sample.fxml"));
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        //move around here
        root.setOnMouseDragged(event -> {
            Main.primaryStage.setX(event.getScreenX() - xOffset);
            Main.primaryStage.setY(event.getScreenY() - yOffset);
        });
        Main.primaryStage = primaryStage;
        Main.primaryStage.initStyle(StageStyle.UNDECORATED);
        Main.primaryStage.setTitle("E-Gov");
        Main.primaryStage.setScene(new Scene(root, 980, 740));
        Main.primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void cardChangedEvent(ReadingStatus status) {
        System.out.println("cardChangedEvent "+status);
        if (ReadingStatus.READ.equals(status)) {
            System.out.println(citizenCard.getCardData());
        } else {
        }
    }

    @Override
    public void cardReadEvent(CardData citizenCardData) {
        System.out.println("cardReadEvent "+citizenCardData);
    }
}
