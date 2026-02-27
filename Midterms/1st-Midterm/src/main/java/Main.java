import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import up.edu.cg.ui.MainFrame;
import up.edu.cg.controller.ImageController;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

        MainFrame mainFrame = new MainFrame();

        Scene scene = new Scene(mainFrame, 1000, 700);

        primaryStage.setTitle("Image editor");
        primaryStage.setScene(scene);
        primaryStage.show();

        new ImageController(mainFrame);
    }

}