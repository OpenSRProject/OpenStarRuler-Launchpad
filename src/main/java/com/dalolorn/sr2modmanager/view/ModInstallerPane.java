package com.dalolorn.sr2modmanager.view;

import com.dalolorn.sr2modmanager.adapter.ModInstaller;
import com.dalolorn.sr2modmanager.adapter.Recommendation;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class ModInstallerPane extends GridPane {
	@FXML Button connectButton;
	@FXML Button installButton;
	@FXML TextField urlField;
	@FXML Label urlLabel;
	@FXML Label recommendationLabel;
	@FXML TextArea modInfo;
	@FXML TextArea branchInfo;
	@FXML ListView<String> branchList;
	@FXML ListView<String> recommendationList;

	@FXML
	public void initialize() {
		branchList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> setActiveBranch(newValue));
		recommendationList.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> urlField.setText(newValue.intValue() >= 0 ? recommendationList.getItems().get(newValue.intValue()) : ""));

		try {
			Recommendation.load();
			recommendationList.getItems().addAll(Recommendation.getInstance().getRecommendationList());
		} catch (IOException e) {
			var msg = new Alert(Alert.AlertType.WARNING, "Could not load history.json! Cause:"  + e);
			e.printStackTrace();
			msg.showAndWait();
		}
	}

	private void setActiveBranch(@NotNull String branchName) {
		branchInfo.setText(ModInstaller.setActiveBranch(branchName));
		installButton.setDisable(!ModInstaller.hasBranch());
	}

	@FXML private void connectToRepository(ActionEvent event) {
		MainController.executeTask("Loading repository...", taskUpdateStage -> new Task<>() {
			@Override
			protected Void call() {
				String repoURL;
				ModInstaller.TextHandler errorHandler = error -> Platform.runLater(() -> {
					var msg = new ResizableAlert(Alert.AlertType.ERROR, error);
					msg.show();
				});

				if (
						(repoURL = ModInstaller.connectToRepository
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
					setUrlText("Connected to " + repoURL);
					try {
						Recommendation.getInstance().addItem(urlField.getText());
						recommendationList.getItems().setAll(Recommendation.getInstance().getRecommendationList());
					} catch (IOException e) {
						var msg = new Alert(Alert.AlertType.WARNING, "Could not save history.json! Cause:"  + e);
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
		});
	}

	@FXML private void installMod(ActionEvent event) {
		MainController.executeTask("Preparing to install mod...", taskInstallStage -> new Task<>() {
			@Override
			protected Void call() {
				updateMessage("Preparing to install mod...");
				ModInstaller.installMod(
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
		});
	}

	boolean getBranches(ModInstaller.TextHandler errorHandler) {
		List<String> tagNames = ModInstaller.getBranches(errorHandler);
		if(tagNames == null) return false;

		Platform.runLater(() -> {
			branchList.getItems().clear();
			branchList.getItems().addAll(tagNames);
		});
		return true;
	}

	boolean getDescription(ModInstaller.TextHandler errorHandler) {
		String description = ModInstaller.getDescription(errorHandler);
		if(description == null) return false;

		Platform.runLater(() -> {
			modInfo.setText(description);

			branchInfo.setText("No branches selected");
		});
		return true;
	}

	void setUrlText(String url) {
		urlLabel.setText(url);
	}

	void deleteRepository() {
		var dialog = new ResizableAlert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this repository from your computer? You will not be able to access it again until you redownload it.");
		ObservableList<ButtonType> dlgButtons = dialog.getDialogPane().getButtonTypes();
		dlgButtons.clear();
		dlgButtons.add(ButtonType.YES);
		dlgButtons.add(ButtonType.NO);
		dialog.setHeaderText("Delete Repository?");

		dialog.showAndWait()
				.filter(response -> response == ButtonType.YES)
				.ifPresent(response -> {
					if(ModInstaller.deleteRepository(error -> {
						var msg = new ResizableAlert(Alert.AlertType.ERROR, error);
						msg.show();
					})) {
						branchList.getItems().clear();
						modInfo.setText("");
						branchInfo.setText("");
					}
				});
	}

	void uninstallMod() {
		var dialog = new ResizableAlert(Alert.AlertType.CONFIRMATION, "Are you sure you want to uninstall this mod? The associated repository will not be removed from your computer.");
		ObservableList<ButtonType> dlgButtons = dialog.getDialogPane().getButtonTypes();
		dlgButtons.clear();
		dlgButtons.add(ButtonType.YES);
		dlgButtons.add(ButtonType.NO);
		dialog.setHeaderText("Uninstall Mod?");

		dialog.showAndWait()
				.filter(response -> response == ButtonType.YES)
				.ifPresent(response -> ModInstaller.uninstallMod(error -> {
					var msg = new ResizableAlert(Alert.AlertType.ERROR, error);
					msg.show();
				}));
	}
}
