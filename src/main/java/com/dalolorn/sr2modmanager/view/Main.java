package com.dalolorn.sr2modmanager.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		try {

			Parent root = FXMLLoader.load(getClass().getResource("MainFrame.fxml"));

			var scene = new Scene(root);

			primaryStage.setTitle("SR2 Mod Manager");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			var msg = new ResizableAlert(Alert.AlertType.ERROR, "Cannot load GUI, application will now terminate.");
			e.printStackTrace();
			msg.showAndWait().ifPresent(response -> System.exit(-1));
		}
	}
}