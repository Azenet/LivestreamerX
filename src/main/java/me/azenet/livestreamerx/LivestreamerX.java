package me.azenet.livestreamerx;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import com.sun.javafx.application.HostServicesDelegate;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LivestreamerX {
	private static      LivestreamerX instance               = new LivestreamerX();
	private             boolean       acceptedLicense        = false;
	private             Main          application            = null;
	private             File          licenseFile            = new File("livestreamerx/license-accepted");
	private             File          vlcFile                = null;
	private             File          livestreamerFile       = null;
	private             File          vlcPathConfig          = new File("livestreamerx/vlc-path");
	private             File          livestreamerPathConfig = new File("livestreamerx/livestreamer-path");
	public static final String        VERSION                = "1.0.1";

	public File getVlcFile() {
		return vlcFile;
	}

	public File getLivestreamerFile() {
		return livestreamerFile;
	}

	public static LivestreamerX getInstance() {
		return instance;
	}

	private LivestreamerX() {
		(new File("livestreamerx")).mkdirs();
		acceptedLicense = licenseFile.exists();
		detectTools();
	}

	public void detectTools() {
		if (vlcPathConfig.exists()) {
			try {
				vlcFile = new File(FileUtils.readFileToString(vlcPathConfig).trim());
			} catch (IOException e) {
				vlcFile = null;
			}
		}

		if (livestreamerPathConfig.exists()) {
			try {
				livestreamerFile = new File(FileUtils.readFileToString(livestreamerPathConfig).trim());
			} catch (IOException e) {
				livestreamerFile = null;
			}
		}

		if (null == vlcFile) {
			vlcFile = findVlc();
		}

		if (null == livestreamerFile) {
			livestreamerFile = findLivestreamer();
		}

		if (null != vlcFile && null != livestreamerFile) {
			setVlcAndLivestreamer(vlcFile, livestreamerFile);
		}
	}

	public File findVlc() {
		File f = null;

		for (String s : new String[]{"vlc", "vlc.exe"}) {
			f = findInPath(s);
			if (f != null) {
				break;
			}
		}

		if (f == null) {
			for (String s : new String[]{"C:\\Program Files\\VideoLAN\\VLC\\vlc.exe", "C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe", "/Applications/VLC.app"}) {
				f = new File(s);
				if (f.exists()) {
					break;
				}
				f = null;
			}
		}

		return f;
	}

	public File findLivestreamer() {
		File f = null;

		for (String s : new String[]{"livestreamer", "livestreamer.exe"}) {
			f = findInPath(s);
			if (f != null) {
				break;
			}
		}

		if (f == null) {
			for (String s : new String[]{"C:\\Program Files\\Livestreamer\\livestreamer.exe", "C:\\Program Files (x86)\\Livestreamer\\livestreamer.exe"}) {
				f = new File(s);
				if (f.exists()) {
					break;
				}
				f = null;
			}
		}

		return f;
	}

	private File findInPath(String exec) {
		File result = null;

		String[] paths = System.getenv("PATH").split(File.pathSeparator);
		for (String s : paths) {
			File f = new File(s + (s.charAt(s.length() - 1) == File.separatorChar ? "" : File.separator) + exec);
			if (f.exists()) {
				result = f;
				break;
			}
		}

		return result;
	}

	public void setApplication(Main application) {
		this.application = application;
	}

	public void acceptLicense() {
		acceptedLicense = true;
		try {
			licenseFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}

	public void showNextGui() {
		showNextGui(new Stage());
	}

	public void showNextGui(Stage stage) {
		stage.setTitle("LivestreamerX");
		if (!acceptedLicense) {
			try {
				application.askForLivestreamerLicense(stage);
				return;
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

		if (vlcFile == null || livestreamerFile == null) {
			detectTools();
		}

		if (vlcFile == null || livestreamerFile == null) {
			try {
				application.showAppLocator(stage);
				return;
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(3);
			}
		}

		try {
			application.showMain(stage);
			return;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(4);
		}
	}

	public void setVlcAndLivestreamer(File vlcFile, File livestreamerFile) {
		try {
			FileUtils.write(vlcPathConfig, vlcFile.toString());
			FileUtils.write(livestreamerPathConfig, livestreamerFile.toString());

			this.vlcFile = vlcFile;
			this.livestreamerFile = livestreamerFile;
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(4);
		}
	}

	public void openUrl(String s) {
		HostServicesDelegate hostServices = HostServicesFactory.getInstance(application);
		hostServices.showDocument(s);
	}

	public void exit() {
		System.exit(0);
	}

	public void writeLivestreamerConfig(File config) {
		List<String> lines = new ArrayList<>();
		lines.add("player=\"" + vlcFile.toString() + "\" --play-and-exit");

		try {
			FileUtils.writeLines(config, lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void checkForUpdates() {
		(new Thread(() -> {
			try {
				URL updateUrl = new URL("https://api.github.com/repos/Azenet/LivestreamerX/releases/latest");
				HttpsURLConnection hurlc = (HttpsURLConnection) updateUrl.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(hurlc.getInputStream()));
				JSONParser p = new JSONParser();
				JSONObject o = (JSONObject) p.parse(in);

				if (o.containsKey("name") && !o.get("name").equals(LivestreamerX.VERSION)) {
					Platform.runLater(() -> LivestreamManager.getInstance().getStream().setStatus("New version " + o.get("name") + " is available for download. Go to http://bit.ly/21b36ki to fetch it."));
				}
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		})).start();
	}
}
