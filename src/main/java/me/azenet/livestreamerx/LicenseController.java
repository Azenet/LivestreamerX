package me.azenet.livestreamerx;

import javafx.scene.control.Button;
import javafx.stage.Stage;

public class LicenseController {
	public Button yesButton;
	public Button noButton;

	public void onNoClicked() {
		System.exit(0);
	}

	public void onYesClicked() throws Exception {
		((Stage) yesButton.getScene().getWindow()).close();
		LivestreamerX.getInstance().acceptLicense();
		LivestreamerX.getInstance().showNextGui();
	}
}
