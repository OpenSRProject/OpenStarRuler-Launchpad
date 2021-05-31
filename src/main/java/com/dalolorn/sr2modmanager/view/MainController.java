package com.dalolorn.sr2modmanager.view;

import com.dalolorn.sr2modmanager.adapter.Recommendation;
import com.dalolorn.sr2modmanager.adapter.RepositoryManager;
import com.dalolorn.sr2modmanager.adapter.Settings;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.*;

import java.io.*;
import java.util.List;

public class MainController {
	@FXML private Button connectButton;
	@FXML private Button installButton;
	@FXML private TextField urlField;
	@FXML private Label urlLabel;
	@FXML private Label recommendationLabel;
	@FXML private TextArea modInfo, branchInfo;
	@FXML private ListView<String> branchList;
	@FXML private ListView<String> recommendationList;

	public void initialize() {
		branchList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> setActiveBranch(newValue));
		recommendationList.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> urlField.setText(newValue.intValue() >= 0 ? recommendationList.getItems().get(newValue.intValue()) : ""));

		boolean needsConfig = true;
		try {
			needsConfig = !Settings.load();
		} catch(IOException e) {
			Alert msg = new ResizableAlert(Alert.AlertType.ERROR, "Could not load config.json! Cause:"  + e.toString());
			e.printStackTrace();
			msg.showAndWait();
		}
		if(needsConfig || (!new File(Settings.getInstance().gamePath, "Star Ruler 2.exe").exists() && !new File(Settings.getInstance().gamePath, "StarRuler2.sh").exists())) {
			Alert msg = new ResizableAlert(Alert.AlertType.WARNING, "Could not detect Star Ruler 2 launchers! Please navigate to the root folder of your SR2 installation, containing the files 'Star Ruler 2.exe' and/or 'StarRuler2.sh'!");
			msg.showAndWait();
			setSR2Path((Window) null);
		}

		try {
			Recommendation.load();
			recommendationList.getItems().addAll(Recommendation.getInstance().getRecommendationList());
		} catch (IOException e) {
			Alert msg = new Alert(Alert.AlertType.WARNING, "Could not load history.json! Cause:"  + e.toString());
			e.printStackTrace();
			msg.showAndWait();
		}
	}

	@FXML private void setSR2Path(ActionEvent event) {
		setSR2Path(new File(Settings.getInstance().gamePath), urlField.getScene().getWindow());
	}

	private void setSR2Path(Window window) {
		setSR2Path(new File(Settings.getInstance().gamePath), window);
	}

	private void setSR2Path(File initialDirectory, Window window) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setInitialDirectory(initialDirectory);
		if(!chooser.getInitialDirectory().exists())
			chooser.setInitialDirectory(new File("."));
		chooser.setTitle("Set SR2 Path");
		File dir = chooser.showDialog(window);
		if(dir == null || !dir.exists())
			return;
		File winLauncher = new File(dir, "Star Ruler 2.exe");
		File linuxLauncher = new File(dir, "StarRuler2.sh");
		if(!winLauncher.exists() && !linuxLauncher.exists()) {
			Alert msg = new ResizableAlert(Alert.AlertType.ERROR, "This is not the root directory of a Star Ruler 2 installation!");
			msg.showAndWait();
			setSR2Path(dir, window);
			return;
		}

		Settings.getInstance().gamePath = dir.getAbsolutePath();
		try {
			Settings.getInstance().save();
		} catch (IOException e) {
			Alert msg = new ResizableAlert(Alert.AlertType.WARNING, "Failed to save config file!");
			msg.show();
			e.printStackTrace();
		}
	}

	private void setActiveBranch(String branchName) {
		branchInfo.setText(RepositoryManager.setActiveBranch(branchName));
		installButton.setDisable(RepositoryManager.currentBranch == null);
	}

	@FXML private void connectToRepository(ActionEvent event) {
		// Start preparing the download task.
		final double wndwWidth = 400.0d;
		Label updateLabel = new Label("Loading repository...");
		updateLabel.setPrefWidth(wndwWidth-20);
		updateLabel.setWrapText(true);
		ProgressBar progress = new ProgressBar();
		progress.setPrefWidth(wndwWidth);

		VBox updatePane = new VBox();
		updatePane.setPadding(new Insets(10, 10, 30, 10));
		updatePane.setSpacing(5.0d);
		updatePane.getChildren().addAll(updateLabel, progress);

		Stage taskUpdateStage = new Stage(StageStyle.UTILITY);
		taskUpdateStage.initModality(Modality.APPLICATION_MODAL);
		taskUpdateStage.setScene(new Scene(updatePane));
		taskUpdateStage.show();

		Task updateTask = new Task<Void>() {
			@Override
			protected Void call() {
				String repoURL;
				RepositoryManager.TextHandler errorHandler = error -> Platform.runLater(() -> {
					Alert msg = new ResizableAlert(Alert.AlertType.ERROR, error);
					msg.show();
				});

				if (
					(repoURL = RepositoryManager.connectToRepository
						(
							urlField.getText(),
							this::updateMessage,
							errorHandler
						)
					) == null
				) return null;

				updateMessage("Fetching branch list...");
				if(!getBranches(errorHandler)) return null;

				updateMessage("Getting mod description...");
				if(!getDescription(errorHandler)) return null;

				Platform.runLater(() -> {
					urlLabel.setText("Connected to " + repoURL);
					try {
						Recommendation.getInstance().addItem(repoURL);
						recommendationList.getItems().setAll(Recommendation.getInstance().getRecommendationList());
					} catch (IOException e) {
						Alert msg = new Alert(Alert.AlertType.WARNING, "Could not save history.json! Cause:"  + e.toString());
						e.printStackTrace();
						msg.showAndWait();
					}
				});
				return null;
			}

			@Override
			protected void succeeded() {
				taskUpdateStage.close();
			}
		};

		progress.progressProperty().bind(updateTask.progressProperty());
		updateLabel.textProperty().bind(updateTask.messageProperty());

		taskUpdateStage.show();
		new Thread(updateTask).start();
	}

	@FXML private void installMod(ActionEvent event) {
		// Start preparing the install task.
		final double wndwWidth = 400.0d;
		Label installLabel = new Label("Preparing to install mod...");
		installLabel.setPrefWidth(wndwWidth-20);
		installLabel.setWrapText(true);
		ProgressBar progress = new ProgressBar();
		progress.setPrefWidth(wndwWidth);

		VBox installPane = new VBox();
		installPane.setPadding(new Insets(10, 10, 30, 10));
		installPane.setSpacing(5.0d);
		installPane.getChildren().addAll(installLabel, progress);

		Stage taskInstallStage = new Stage(StageStyle.UTILITY);
		taskInstallStage.initModality(Modality.APPLICATION_MODAL);
		taskInstallStage.setScene(new Scene(installPane));
		taskInstallStage.show();
		Task installTask = new Task<Void>() {
			@Override
			protected Void call() {
				updateMessage("Preparing to install mod...");
				RepositoryManager.installMod(
						warning -> Platform.runLater(() -> {
							Alert msg = new ResizableAlert(Alert.AlertType.WARNING, warning);
							msg.show();
						}),
						this::updateMessage,
						info -> Platform.runLater(() -> {
							Alert msg = new ResizableAlert(Alert.AlertType.INFORMATION, info);
							msg.show();
						}),
						error -> Platform.runLater(() -> {
							Alert msg = new ResizableAlert(Alert.AlertType.ERROR, error);
							msg.show();
						})
				);
				return null;
			}

			@Override
			protected void succeeded() {
				taskInstallStage.close();
			}
		};

		progress.progressProperty().bind(installTask.progressProperty());
		installLabel.textProperty().bind(installTask.messageProperty());

		taskInstallStage.show();
		new Thread(installTask).start();
	}

	private boolean getBranches(RepositoryManager.TextHandler errorHandler) {
		List<String> tagNames = RepositoryManager.getBranches(errorHandler);
		if(tagNames == null) return false;

		Platform.runLater(() -> {
			branchList.getItems().clear();
			branchList.getItems().addAll(tagNames);
		});
		return true;
	}

	private boolean getDescription(RepositoryManager.TextHandler errorHandler) {
		String description = RepositoryManager.getDescription(errorHandler);
		if(description == null) return false;

		Platform.runLater(() -> {
			modInfo.setText(description);

			branchInfo.setText("No branches selected");
		});
		return true;
	}

	@FXML private void openRepository(ActionEvent actionEvent) {
		// Get repository location.
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setInitialDirectory(new File("repositories/"));
		chooser.getInitialDirectory().mkdirs();
		chooser.setTitle("Choose a Repository");
		final File dir = chooser.showDialog(urlField.getScene().getWindow()); // Anything on the window would suffice, urlField was just arbitrarily selected.
		if(dir == null)
			return;

		// Start preparing the download task.
		final double wndwWidth = 400.0d;
		Label updateLabel = new Label("Loading repository...");
		updateLabel.setPrefWidth(wndwWidth-20);
		updateLabel.setWrapText(true);
		ProgressBar progress = new ProgressBar();
		progress.setPrefWidth(wndwWidth);

		VBox updatePane = new VBox();
		updatePane.setPadding(new Insets(10, 10, 30, 10));
		updatePane.setSpacing(5.0d);
		updatePane.getChildren().addAll(updateLabel, progress);

		Stage taskUpdateStage = new Stage(StageStyle.UTILITY);
		taskUpdateStage.initModality(Modality.APPLICATION_MODAL);
		taskUpdateStage.setScene(new Scene(updatePane));
		taskUpdateStage.show();

		Task updateTask = new Task<Void>() {
			@Override
			protected Void call() {
				String repoURL;
				RepositoryManager.TextHandler errorHandler = error -> Platform.runLater(() -> {
					Alert msg = new ResizableAlert(Alert.AlertType.ERROR, error);
					msg.show();
				});

				if (
						(repoURL = RepositoryManager.openRepository
								(
										dir,
										errorHandler
								)
						) == null
				) return null;

				updateMessage("Fetching branch list...");
				if(!getBranches(errorHandler)) return null;

				updateMessage("Getting mod description...");
				if(!getDescription(errorHandler)) return null;

				Platform.runLater(() -> urlLabel.setText("Connected to " + repoURL));
				return null;
			}

			@Override
			protected void succeeded() {
				taskUpdateStage.close();
			}
		};

		progress.progressProperty().bind(updateTask.progressProperty());
		updateLabel.textProperty().bind(updateTask.messageProperty());

		taskUpdateStage.show();
		new Thread(updateTask).start();
	}

	@FXML private void close(ActionEvent actionEvent) {
		Alert dialog = new ResizableAlert(Alert.AlertType.CONFIRMATION, "Quit SR2 Mod Manager?");
		ObservableList<ButtonType> dlgButtons = dialog.getDialogPane().getButtonTypes();
		dlgButtons.clear();
		dlgButtons.add(ButtonType.YES);
		dlgButtons.add(ButtonType.NO);
		dialog.setHeaderText("Quit SR2MM");

		dialog.showAndWait()
				.filter(response -> response == ButtonType.YES)
				.ifPresent(response -> System.exit(0));
	}

	@FXML private void deleteRepository(ActionEvent actionEvent) {
		Alert dialog = new ResizableAlert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this repository from your computer? You will not be able to access it again until you redownload it.");
		ObservableList<ButtonType> dlgButtons = dialog.getDialogPane().getButtonTypes();
		dlgButtons.clear();
		dlgButtons.add(ButtonType.YES);
		dlgButtons.add(ButtonType.NO);
		dialog.setHeaderText("Delete Repository?");

		dialog.showAndWait()
				.filter(response -> response == ButtonType.YES)
				.ifPresent(response -> {
						if(RepositoryManager.deleteRepository(error -> {
							Alert msg = new ResizableAlert(Alert.AlertType.ERROR, error);
							msg.show();
						})) {
							branchList.getItems().clear();
							modInfo.setText("");
							branchInfo.setText("");
						}
				});
	}

	@FXML private void about(ActionEvent actionEvent) {
		Alert msg = new ResizableAlert(Alert.AlertType.INFORMATION,
				"SR2 Mod Manager by Dalo Lorn\n" +
						"\n" +
						"Version: 1.1.1\n\n" +
						"GitHub: https://github.com/DaloLorn/SR2ModManager\n" +
						"Discord: https://discord.gg/sUJKJDc\n" +
						"Patreon: https://patreon.com/rising_stars_sr2");
		msg.setHeaderText("About SR2 Mod Manager");
		msg.show();
	}
}
