package com.dalolorn.sr2modmanager.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class ModEntry extends BorderPane {
	@FXML private CheckBox enabledToggle;
	@FXML private Button updateButton;
	@FXML private Button uninstallButton;
	@FXML private Button openButton;

	public static ModEntry buildEntry() throws IOException {
		var loader = new FXMLLoader(ModEntry.class.getResource("ModEntry.fxml"));

		return loader.load();
	}

	@FXML
	public void initialize() {

	}

	public void toggleMod(ActionEvent actionEvent) {

	}

	public void updateCurrentBranch(ActionEvent actionEvent) {

	}

	public void openMod(ActionEvent actionEvent) {

	}
}
