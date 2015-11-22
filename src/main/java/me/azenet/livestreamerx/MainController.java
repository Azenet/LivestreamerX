package me.azenet.livestreamerx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import me.azenet.livestreamerx.model.Stream;

public class MainController {
	public  TextField         streamLink;
	public  ChoiceBox<String> qualityChoice;
	public  Label             status;
	public  Button            startStreamButton;
	public  Button            loadQualitiesButton;
	private Stream            stream;

	@FXML
	public void initialize() {
		stream = LivestreamManager.getInstance().getStream();
		LivestreamManager.getInstance().setMainController(this);

		streamLink.setText(stream.getStreamLink());
		stream.streamLinkProperty().bind(streamLink.textProperty());
		stream.qualitiesProperty().bind(qualityChoice.itemsProperty());
		stream.preferredQualityProperty().bind(qualityChoice.valueProperty());
		status.textProperty().bind(stream.statusProperty());

		LivestreamerX.getInstance().checkForUpdates();
	}

	public void onLoadQualitiesClicked() {
		loadQualitiesButton.setDisable(true);
		startStreamButton.setDisable(true);
		stream.setStatus("Loading qualities...");
		stream.getQualities().clear();
		LivestreamManager.getInstance().loadQualities();
	}

	public void onLinkEdit() {
		if (!LivestreamManager.getInstance().isStreamRunning()) {
			startStreamButton.setDisable(true);
			loadQualitiesButton.setDisable(false);
			qualityChoice.setDisable(true);
			stream.getQualities().clear();
		}
	}

	public void startOrStopStreamClicked() {
		LivestreamManager.getInstance().startStream();
	}

	public void exit(ActionEvent actionEvent) {
		LivestreamerX.getInstance().exit();
	}
}
