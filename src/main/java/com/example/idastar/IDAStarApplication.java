package com.example.idastar;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class IDAStarApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(IDAStarApplication.class.getResource("game-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 760);
        stage.setResizable(false);
        stage.setTitle("Ghép tranh");
        stage.getIcons().add(new Image(Objects.requireNonNull(IDAStarApplication.class.getResourceAsStream("img/monalisa.jpg"))));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
