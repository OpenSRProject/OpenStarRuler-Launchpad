package com.dalolorn.sr2modmanager.view;

import com.dalolorn.sr2modmanager.adapter.ModInstaller;
import com.dalolorn.sr2modmanager.adapter.Settings;
import com.dalolorn.sr2modmanager.adapter.Utils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

public class MainController {
	// Shared UI components.
	@FXML private Button playButton;
	@FXML private TabPane tabs;
	@FXML private Tab modsTab;
	@FXML private Tab installerTab;
	@FXML private Tab osrTab;

	@FXML private MenuItem openRepositoryItem;
	@FXML private MenuItem deleteRepositoryItem;
	@FXML private MenuItem uninstallModItem;

	@FXML private ModManagerPane modsPane;
	@FXML private ModInstallerPane installerPane;
	@FXML private OpenSRManagerPane osrPane;

	public void initialize() {
		tabs.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
			deleteRepositoryItem.setDisable(newTab != installerTab || !ModInstaller.hasRepo());
			uninstallModItem.setDisable(newTab != installerTab);
		});
		osrPane.playButton = playButton;

		var needsConfig = true;
		try {
			needsConfig = !Settings.load();
		} catch(IOException e) {
			Alert msg = new ResizableAlert(Alert.AlertType.ERROR, "Could not load config.json! Cause:"  + e);
			e.printStackTrace();
			msg.showAndWait();
		}
		if(needsConfig || (!new File(Settings.getInstance().gamePath, "Star Ruler 2.exe").exists() && !new File(Settings.getInstance().gamePath, "StarRuler2.sh").exists())) {
			var msg = new ResizableAlert(Alert.AlertType.WARNING, "Could not detect Star Ruler 2 launchers! Please navigate to the root folder of your SR2 installation, containing the files 'Star Ruler 2.exe' and/or 'StarRuler2.sh'!");
			msg.showAndWait();
			setSR2Path(null);
		}
	}

	@FXML private void setSR2Path(ActionEvent event) {
		osrPane.setSR2Path(event);
	}

	public static void executeTask(
			@NotNull String initialText,
			@NotNull Function<Stage, Task<Void>> taskBuilder
	) {
		// Start preparing the task.
		final var wndwWidth = 400.0d;
		var taskLabel = new Label(initialText);
		taskLabel.setPrefWidth(wndwWidth-20);
		taskLabel.setWrapText(true);
		var progress = new ProgressBar();
		progress.setPrefWidth(wndwWidth);

		var taskPane = new VBox();
		taskPane.setPadding(new Insets(10, 10, 30, 10));
		taskPane.setSpacing(5.0d);
		taskPane.getChildren().addAll(taskLabel, progress);

		var taskStage = new Stage(StageStyle.UTILITY);
		taskStage.initModality(Modality.APPLICATION_MODAL);
		taskStage.setScene(new Scene(taskPane));
		taskStage.show();

		Task<Void> task = taskBuilder.apply(taskStage);

		progress.progressProperty().bind(task.progressProperty());
		taskLabel.textProperty().bind(task.messageProperty());

		taskStage.show();
		new Thread(task).start();
	}

	@FXML private void openRepository(ActionEvent actionEvent) {
		// Get repository location.
		var chooser = new DirectoryChooser();
		chooser.setInitialDirectory(new File("repositories/"));
		chooser.getInitialDirectory().mkdirs();
		chooser.setTitle("Choose a Repository");
		final File dir = chooser.showDialog(playButton.getScene().getWindow()); // Anything on the window would suffice, playButton was just arbitrarily selected.
		if(dir == null)
			return;

		executeTask("Loading repository...", taskUpdateStage -> new Task<>() {
			@Override
			protected Void call() {
				String repoURL;
				ModInstaller.TextHandler errorHandler = error -> Platform.runLater(() -> {
					Alert msg = new ResizableAlert(Alert.AlertType.ERROR, error);
					msg.show();
				});

				if (
						(repoURL = ModInstaller.openRepository
								(
										dir,
										errorHandler
								)
						) == null
				) return null;

				updateMessage("Fetching branch list...");
				if(!installerPane.getBranches(errorHandler)) return null;

				updateMessage("Getting mod description...");
				if(!installerPane.getDescription(errorHandler)) return null;

				Platform.runLater(() -> installerPane.setUrlText("Connected to " + repoURL));
				return null;
			}

			@Override
			protected void succeeded() {
				taskUpdateStage.close();
			}
		});
	}

	@FXML private void close(ActionEvent actionEvent) {
		var dialog = new ResizableAlert(Alert.AlertType.CONFIRMATION, "Quit OpenSR Launchpad?");
		ObservableList<ButtonType> dlgButtons = dialog.getDialogPane().getButtonTypes();
		dlgButtons.clear();
		dlgButtons.add(ButtonType.YES);
		dlgButtons.add(ButtonType.NO);
		dialog.setHeaderText("Quit OpenSR Launchpad");

		dialog.showAndWait()
				.filter(response -> response == ButtonType.YES)
				.ifPresent(response -> System.exit(0));
	}

	@FXML private void deleteRepository(ActionEvent actionEvent) {
		if(installerTab.isSelected()) {
			installerPane.deleteRepository();
		}
	}

	@FXML private void about(ActionEvent actionEvent) {
		Alert msg = new ResizableAlert(Alert.AlertType.INFORMATION,
				"OpenSR Launchpad\n" +
						"\n" +
						"Version: 2.0.0\n\n" +
						"GitHub: https://github.com/OpenSRProject/OpenStarRuler-Launchpad\n" +
						"Discord: https://discord.gg/sUJKJDc");
		msg.setHeaderText("About OpenSR Launchpad");
		msg.show();
	}

	@FXML private void startGame(ActionEvent actionEvent) {
		var launcher = new File(Settings.getInstance().gamePath, Utils.IS_WINDOWS ? "Star Ruler 2.exe" : "StarRuler2.sh");
		if(!launcher.exists())
			new ResizableAlert(Alert.AlertType.ERROR,
					"Cannot start Star Ruler 2: Couldn't find platform-appropriate loader!")
					.showAndWait();

		if(!launcher.canExecute())
			new ResizableAlert(Alert.AlertType.ERROR,
					"Cannot start Star Ruler 2: Couldn't run game loader!")
					.showAndWait();

		try {
			new ProcessBuilder(launcher.getAbsolutePath())
				.directory(launcher.getParentFile())
				.start();
		} catch (Exception e) {
			var msg = new ResizableAlert(Alert.AlertType.ERROR,
					String.format("Cannot start Star Ruler 2: %s", e));
			e.printStackTrace();
			msg.showAndWait();
		}
	}

	@FXML public void uninstallMod(ActionEvent event) {
		if(installerTab.isSelected()) {
			installerPane.uninstallMod();
		}
	}
}
