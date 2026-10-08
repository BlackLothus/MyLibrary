package it.unisa.mylibrary;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        // Risoluzione minima richiesta dal requisito RNF07
        Scene scene = new Scene(root, 1280, 720);

        primaryStage.setTitle("MyLibrary - Setup");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}