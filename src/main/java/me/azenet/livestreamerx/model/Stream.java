package me.azenet.livestreamerx.model;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Stream {
	private SimpleStringProperty       streamLink       = new SimpleStringProperty();
	private SimpleStringProperty       preferredQuality = new SimpleStringProperty();
	private SimpleStringProperty       status           = new SimpleStringProperty("Enter a stream link, e.g. twitch.tv/esl_csgo or mlg.tv/cevo.");
	private SimpleListProperty<String> qualities        = new SimpleListProperty<>(FXCollections.observableArrayList("Click \"Load qualities\""));

	public Stream() { }

	public String getStreamLink() {
		return streamLink.get();
	}

	public SimpleStringProperty streamLinkProperty() {
		return streamLink;
	}

	public void setStreamLink(String streamLink) {
		this.streamLink.set(streamLink);
	}

	public String getPreferredQuality() {
		return preferredQuality.get();
	}

	public SimpleStringProperty preferredQualityProperty() {
		return preferredQuality;
	}

	public void setPreferredQuality(String preferredQuality) {
		this.preferredQuality.set(preferredQuality);
	}

	public String getStatus() {
		return status.get();
	}

	public SimpleStringProperty statusProperty() {
		return status;
	}

	public void setStatus(String status) {
		this.status.set(status);
	}

	public ObservableList<String> getQualities() {
		return qualities.get();
	}

	public SimpleListProperty<String> qualitiesProperty() {
		return qualities;
	}

	public void setQualities(ObservableList<String> qualities) {
		this.qualities.set(qualities);
	}
}