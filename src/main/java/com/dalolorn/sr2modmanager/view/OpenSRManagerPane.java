package com.dalolorn.sr2modmanager.view;

import com.dalolorn.sr2modmanager.adapter.Settings;
import com.dalolorn.sr2modmanager.adapter.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class OpenSRManagerPane extends GridPane {
	Button playButton;

	@FXML ListView<String> osrVersionList;
	@FXML Label gamePathCaption;
	@FXML Label gamePathLabel;
	@FXML Button findGameButton;
	@FXML Button updateGameButton;
	@FXML TextArea osrVersionInfo;

	@FXML
	public void initialize() {

	}

	@FXML void setSR2Path(ActionEvent event) {
		setSR2Path(getScene().getWindow());
	}

	private void setSR2Path(@NotNull Window window) {
		playButton.setDisable(true);
		setSR2Path(new File(Settings.getInstance().gamePath), window);
	}

	private void setSR2Path(
			@NotNull File initialDirectory,
			@NotNull Window window
	) {
		var chooser = new DirectoryChooser();
		chooser.setInitialDirectory(initialDirectory);
		if(!chooser.getInitialDirectory().exists())
			chooser.setInitialDirectory(new File("."));
		chooser.setTitle("Set SR2 Path");
		File dir = chooser.showDialog(window);
		if(dir == null || !dir.exists())
			return;
		var launcher = new File(dir, Utils.IS_WINDOWS ? "Star Ruler 2.exe" : "StarRuler2.sh");
		if(!launcher.exists()) {
			Alert msg = new ResizableAlert(Alert.AlertType.ERROR, "This is not the root directory of a Star Ruler 2 installation!");
			msg.showAndWait();
			setSR2Path(dir, window);
			return;
		}

		Settings.getInstance().gamePath = dir.getAbsolutePath();
		playButton.setDisable(false);
		try {
			Settings.getInstance().save();
		} catch (IOException e) {
			Alert msg = new ResizableAlert(Alert.AlertType.WARNING, "Failed to save config file!");
			msg.show();
			e.printStackTrace();
		}
	}

	public void installOpenSR(ActionEvent actionEvent) {

	}
}
