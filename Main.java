package com.fantasia;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/fantasia/scenes/update.fxml"));
        primaryStage.setTitle("TwitchBot");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 354, 227));
        primaryStage.show();
        Context.getInstance().setStage(primaryStage);
    }
}
