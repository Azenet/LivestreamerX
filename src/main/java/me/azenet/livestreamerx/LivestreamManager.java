package me.azenet.livestreamerx;

import javafx.application.Platform;
import me.azenet.livestreamerx.exception.InvalidStreamStateException;
import me.azenet.livestreamerx.exception.InvalidStreamUrlException;
import me.azenet.livestreamerx.exception.LivestreamerXException;
import me.azenet.livestreamerx.model.Stream;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LivestreamManager {
	private MainController            mainController = null;
	private Stream                    stream         = null;
	private LivestreamManagerExecutor streamExecutor = null;
	private static LivestreamManager instance;
	private        Thread            streamExecutorThread;

	public static LivestreamManager getInstance() {
		if (null == instance) {
			instance = new LivestreamManager();
		}

		return instance;
	}

	private LivestreamManager() {
		stream = new Stream();
	}

	public Stream getStream() {
		return stream;
	}

	public void loadQualities() {
		if (StringUtils.isEmpty(stream.getStreamLink())) {
			InvalidStreamUrlException e = new InvalidStreamUrlException("URL is empty");
			stream.setStatus("Error loading qualities: " + e.getMessage());
			mainController.loadQualitiesButton.setDisable(false);
			throw e;
		}

		LivestreamManagerExecutor e = new LivestreamManagerExecutor((exitCode, output) -> {
			try {
				if (exitCode != 0) {
					throw new InvalidStreamUrlException("URL is not supported");
				}

				LivestreamManagerExecutor qualityLoad = new LivestreamManagerExecutor((exitCode1, result) -> {
					try {
						String qualities = null;
						for (String s : result) {
							if (s.startsWith("Available streams: ")) {
								qualities = s;
								break;
							}
						}

						if (null == qualities) {
							throw new InvalidStreamUrlException("Stream is offline");
						}

						final String finalQualities = qualities.split("Available streams: ")[1];

						if (StringUtils.isEmpty(finalQualities.trim())) {
							throw new InvalidStreamUrlException("Qualities could not be parsed");
						}

						Platform.runLater(() -> {
							stream.getQualities().clear();
							for (String quality : finalQualities.split(", ")) {
								quality = quality.trim();
								stream.getQualities().add(quality);
								if (quality.contains("best")) {
									mainController.qualityChoice.valueProperty().set(quality);
								}
							}
							mainController.qualityChoice.setDisable(false);
							mainController.startStreamButton.setDisable(false);
							mainController.loadQualitiesButton.setDisable(false);
							stream.setStatus("Choose a quality and click on \"Start stream\".");
						});
					} catch (final LivestreamerXException ex) {
						Platform.runLater(() -> {
							stream.setStatus("Error loading qualities: " + ex.getMessage());
							mainController.loadQualitiesButton.setDisable(false);
						});
						throw ex;
					}
				}, Arrays.asList(stream.getStreamLink())); //not using -j for easier match for worst and best

				(new Thread(qualityLoad)).start();
			} catch (final LivestreamerXException ex) {
				Platform.runLater(() -> {
					stream.setStatus("Error loading qualities: " + ex.getMessage());
					mainController.loadQualitiesButton.setDisable(false);
				});
				throw ex;
			}
		}, Arrays.asList("--can-handle-url", stream.getStreamLink()));

		(new Thread(e)).start();
	}

	public void startStream() {
		if (isStreamRunning()) {
			throw new InvalidStreamStateException("Stream is already running");
		}

		if (StringUtils.isEmpty(stream.getStreamLink()) || StringUtils.isEmpty(stream.getPreferredQuality()) || !stream.getQualities().contains(stream.getPreferredQuality())) {
			throw new InvalidStreamStateException("Chosen data is invalid");
		}

		String quality = stream.getPreferredQuality();
		if (quality.contains(" ")) {
			quality = quality.split(" ")[0];
		}

		streamExecutor = new LivestreamManagerExecutor((exitCode, result) -> Platform.runLater(() -> {
			stream.setStatus("Stream has been closed/has ended.");
			mainController.startStreamButton.setVisible(true);
			mainController.startStreamButton.setText("Start stream");
			mainController.startStreamButton.setDisable(true);
			try {
				LivestreamManager.getInstance().loadQualities();
			} catch (LivestreamerXException ignored) {}
		}), Arrays.asList(stream.getStreamLink(), quality));

		Platform.runLater(() -> {
			mainController.startStreamButton.setDisable(true);
			stream.setStatus("VLC should open shortly with the requested stream. Close VLC to watch another stream.");
		});
		streamExecutorThread = new Thread(streamExecutor);
		streamExecutorThread.start();
	}

	public boolean isStreamRunning() {
		return streamExecutor != null && streamExecutor.running && streamExecutorThread.isAlive();
	}

	public void setMainController(MainController mainController) {
		this.mainController = mainController;
	}

	private class LivestreamManagerExecutor implements Runnable {
		private final LivestreamManagerExecutorCallback callback;
		private final List<String>                      arguments;
		private Boolean running = false;

		public LivestreamManagerExecutor(LivestreamManagerExecutorCallback callback, List<String> arguments) {
			this.callback = callback;
			this.arguments = arguments;
		}

		public void run() {
			File config = new File("livestreamerx/livestream-config");
			if (!config.exists()) {
				LivestreamerX.getInstance().writeLivestreamerConfig(config);
			}

			ArrayList<String> args = new ArrayList<>();
			args.add(LivestreamerX.getInstance().getLivestreamerFile().getAbsolutePath());
			args.add("--config");
			args.add(config.getAbsolutePath());
			args.addAll(arguments);

			ProcessBuilder pb = new ProcessBuilder(args);
			Process p = null;
			try {
				p = pb.start();
				writeInConsole("Running command: " + String.join(" ", args));

				running = true;

				LivestreamManagerExecutorOutputProcessor lmeop = new LivestreamManagerExecutorOutputProcessor(p.getInputStream());
				(new Thread(lmeop)).start();
				try {
					p.waitFor();
				} catch (InterruptedException ie) {
					p.destroy();
					p.waitFor();
				}

				running = false;

				while (!lmeop.isDoneReading) {
					try {
						Thread.sleep(10L);
					} catch (Exception ignored) {}
				}

				int outCode = p.exitValue();
				writeInConsole("Command exited with code " + outCode + ".");

				try {
					callback.onEnd(outCode, lmeop.collectOutput());
				} catch (LivestreamerXException e) {
					writeInConsole("Error: " + e.getMessage());
				}
			} catch (Exception e) {
				e.printStackTrace();
				running = false;
				try {
					if (p != null) {
						p.destroy();
					}
				} catch (Exception ignored) {}
			}
		}

		private class LivestreamManagerExecutorOutputProcessor implements Runnable {
			private InputStream       is            = null;
			private ArrayList<String> output        = new ArrayList<>();
			private boolean           isDoneReading = false;

			public LivestreamManagerExecutorOutputProcessor(InputStream is) {
				this.is = is;
			}

			public void run() {
				String tmp;
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				try {
					while (null != (tmp = br.readLine())) {
						output.add(tmp);
						writeInConsole(tmp);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					isDoneReading = true;
					try {
						br.close();
						is.close();
					} catch (Exception ignored) {}
				}
			}

			public ArrayList<String> collectOutput() {
				return output;
			}
		}
	}

	public void writeInConsole(String text) {
		System.out.println(text);
	}

	private interface LivestreamManagerExecutorCallback {
		public void onEnd(int exitCode, List<String> result);
	}
}
