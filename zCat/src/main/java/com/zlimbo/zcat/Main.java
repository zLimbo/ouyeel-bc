package com.zlimbo.zcat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends Application {

    /**
     * 日志
     */
    final Logger logger = LoggerFactory.getLogger(getClass());


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.debug("[start] start");
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainWindow.fxml"));
        primaryStage.setTitle("zCat");
        primaryStage.setScene(new Scene(root));
        primaryStage.getIcons().add(new Image(
                getClass().getResourceAsStream("/image/ouyeel_bc.png")));
        primaryStage.show();
        logger.debug("[start] end");
    }
}
