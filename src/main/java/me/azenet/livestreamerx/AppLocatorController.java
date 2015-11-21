package me.azenet.livestreamerx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;

public class AppLocatorController {
	public TextField vlcField;
	public Button    vlcChoose;
	public Button    vlcInstall;
	public TextField livestreamerField;
	public Button    livestreamerChoose;
	public Button    livestreamerInstall;
	public Button    nextButton;

	private File vlcFile;
	private File livestreamerFile;

	@FXML
	public void initialize() {
		rescanFiles();

		if (null != vlcFile && null != livestreamerFile) {
			LivestreamerX.getInstance().setVlcAndLivestreamer(vlcFile, livestreamerFile);
			LivestreamerX.getInstance().showNextGui();
		}
	}

	private void rescanFiles() {
		vlcFile = LivestreamerX.getInstance().findVlc();
		livestreamerFile = LivestreamerX.getInstance().findLivestreamer();

		if (null == vlcFile) {
			vlcFile = LivestreamerX.getInstance().getVlcFile();
		}
		if (null == livestreamerFile) {
			livestreamerFile = LivestreamerX.getInstance().getLivestreamerFile();
		}

		if (null == vlcFile) {
			vlcField.setText("Not found, choose or install");
		} else {
			vlcField.setText(vlcFile.toString());
		}

		if (null == livestreamerFile) {
			livestreamerField.setText("Not found, choose or install");
		} else {
			livestreamerField.setText(livestreamerFile.toString());
		}
	}

	public void checkFileChanges() {
		if (null != vlcFile && null != livestreamerFile) {
			LivestreamerX.getInstance().setVlcAndLivestreamer(vlcFile, livestreamerFile);
			nextButton.setDisable(false);
		}
	}

	public void onVlcChooseClicked() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Choose the path to the VLC executable");
		vlcFile = fc.showOpenDialog(vlcChoose.getScene().getWindow());
		if (null == vlcFile) {
			vlcField.setText("Not found");
		} else {
			vlcField.setText(vlcFile.toString());
			checkFileChanges();
		}
	}

	public void onLivestreamerChooseClicked() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Choose the path to the Livestreamer executable");
		livestreamerFile = fc.showOpenDialog(livestreamerChoose.getScene().getWindow());
		if (null == livestreamerFile) {
			livestreamerField.setText("Not found");
		} else {
			livestreamerField.setText(livestreamerFile.toString());
			checkFileChanges();
		}
	}

	public void onRescanClicked() {
		rescanFiles();
		checkFileChanges();
	}

	public void onNextClicked() {
		if (null != vlcFile && null != livestreamerFile) {
			LivestreamerX.getInstance().setVlcAndLivestreamer(vlcFile, livestreamerFile);
			((Stage) nextButton.getScene().getWindow()).close();
			LivestreamerX.getInstance().showNextGui();
		} else {
			rescanFiles();
		}
	}

	public void onVlcInstallClicked() {
		LivestreamerX.getInstance().openUrl("http://www.videolan.org/vlc/");
		System.exit(0);
	}

	public void onLivestreamerInstallClicked() {
		String url;
		if (SystemUtils.IS_OS_WINDOWS) {
			url = "http://docs.livestreamer.io/install.html#windows-binaries";
		} else {
			url = "http://docs.livestreamer.io/install.html";
		}

		LivestreamerX.getInstance().openUrl(url);
		System.exit(0);
	}
}
