package me.azenet.livestreamerx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) throws Exception {
		LivestreamerX.getInstance().setApplication(this);
		LivestreamerX.getInstance().showNextGui(primaryStage);
	}

	public void askForLivestreamerLicense(Stage stage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/license.fxml"));
		stage.setScene(new Scene(root));
		stage.show();
	}

	public void showAppLocator(Stage stage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/appLocator.fxml"));
		stage.setScene(new Scene(root));
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

	public void showMain(Stage stage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
		stage.setScene(new Scene(root));
		stage.show();
	}
}
