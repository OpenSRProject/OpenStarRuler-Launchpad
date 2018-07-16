package com.dalolorn.sr2modmanager;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MainController {
	@FXML private TextField urlField;
	@FXML private Label urlLabel;
	@FXML private TextArea modInfo, branchInfo;
	@FXML private ListView<String> branchList;

	Git repo;

	Ref currentBranch;
	HashMap<String, Ref> branches = new HashMap<>();
	String gamePath = "..";

	public void initialize() {
		branchList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				setActiveBranch(newValue);
			}
		});
	}

	private void setActiveBranch(String branchName) {
		currentBranch = branches.get(branchName);

		ObjectLoader descriptionLoader;
		try {
			Repository repository = repo.getRepository();
			ObjectId treeId = repository.resolve(currentBranch.getName() + "^{tree}");
			TreeWalk walker = Utils.generateBranchDescWalker(repository, treeId);
			ObjectId descriptionId = walker.getObjectId(0);
			descriptionLoader = repo.getRepository().open(descriptionId);
		} catch (Exception e) {
			branchInfo.setText("No description could be found for this branch.");
			e.printStackTrace();
			return;
		}

		try (
				InputStream descriptionStream = descriptionLoader.openStream();
				BufferedReader descriptionReader = new BufferedReader(new InputStreamReader(descriptionStream));
		) {
			branchInfo.setText(descriptionReader.lines().collect(Collectors.joining("\n")));
		} catch (NullPointerException | IOException e) {
			branchInfo.setText("No description could be found for this branch.");
		}
	}

	@FXML private void connectToRepository(ActionEvent event) {
		try {
			// Get and parse URL, prepare repository root.
			String url = urlField.getText();
			if(!url.startsWith("http"))
				url = "https://" + url;
			if(!url.endsWith(".git"))
				url += ".git";
			String[] path = url.split("/");
			final String repoURL = url;
			String repoName = path[path.length - 2] + "_" + path[path.length - 1];
			repoName = repoName.substring(0, repoName.length() - 4);
			File tempRoot = new File("repositories" + File.separator + repoName);
			if (!tempRoot.exists() || !tempRoot.isDirectory())
				tempRoot = Files.createDirectories(tempRoot.toPath()).toFile();
			final File localRoot = tempRoot;

			// Start preparing the download task.
			final double wndwWidth = 300.0d;
			Label updateLabel = new Label("Loading repository...");
			updateLabel.setPrefWidth(wndwWidth);
			ProgressBar progress = new ProgressBar();
			progress.setPrefWidth(wndwWidth);

			VBox updatePane = new VBox();
			updatePane.setPadding(new Insets(10));
			updatePane.setSpacing(5.0d);
			updatePane.getChildren().addAll(updateLabel, progress);

			Stage taskUpdateStage = new Stage(StageStyle.UTILITY);
			taskUpdateStage.initModality(Modality.APPLICATION_MODAL);
			taskUpdateStage.setScene(new Scene(updatePane));
			taskUpdateStage.show();

			Task updateTask = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					try {
						updateMessage("Loading repository...");
						try {
							repo = Git.open(localRoot);
						}
						catch (IOException e) {
							repo = Git.cloneRepository()
									.setURI(repoURL)
									.setDirectory(localRoot)
									.call();
						}

						updateMessage("Fetching branch list...");
						getBranches();

						updateMessage("Getting mod description...");
						getDescription();
					} catch (Exception e) {
						e.printStackTrace();
						Platform.runLater(() -> {
							Alert msg = new Alert(Alert.AlertType.ERROR, "Encountered an exception: " + e.toString());
							msg.show();
						});
						throw e;
					}
					return null;
				}
			};

			updateTask.setOnSucceeded(t -> {
				taskUpdateStage.close();
				urlLabel.setText("Connected to" + repoURL);
			});
			updateTask.setOnFailed(t -> taskUpdateStage.close());
			progress.progressProperty().bind(updateTask.progressProperty());
			updateLabel.textProperty().bind(updateTask.messageProperty());

			taskUpdateStage.show();
			new Thread(updateTask).start();
		}
		catch(Exception e) {
			Alert msg = new Alert(Alert.AlertType.ERROR, "Encountered an exception: " + e.toString());
			e.printStackTrace();
			msg.show();
		}
	}

	@FXML private void installMod(ActionEvent event) {
		try {
			// Check if we've got this branch already.
			boolean createBranch = repo.getRepository().resolve(currentBranch.getName().replaceFirst("refs/remotes/origin", "refs/heads")) == null;

			repo.checkout()
					.setName(currentBranch.getName().replaceFirst("refs/remotes/origin/", ""))
					.setCreateBranch(createBranch)
					.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
					.setStartPoint(currentBranch.getName())
					.call();

			repo.pull().call();

			File root = repo.getRepository().getWorkTree();

			// Find the folder containing the modinfo file.
			boolean inRoot = false;
			String mod = null;
			Finder finder = new Finder("modinfo.txt");
			Files.walkFileTree(root.getAbsoluteFile().toPath(), finder);
			if(finder.getResult() != null) {
				mod = finder.getResult().getParent().getFileName().toString();
				if(mod.equalsIgnoreCase(root.toPath().getFileName().toString())) {
					inRoot = true; // The modinfo is in the repository root, so we can't discard metadata.
					Alert msg = new Alert(Alert.AlertType.WARNING, "WARNING: Unable to discard repository metadata!\n\nTo improve loading times, it is recommended that you delete the installed mod's .git folder once installation is completed.");
					msg.show();
				}
			}
			else {
				Alert msg = new Alert(Alert.AlertType.ERROR, "Cannot find modinfo.txt!\n\nThis repository does not appear to contain a valid Star Ruler 2 mod.\nPlease make sure that you have connected to the right repository, and contact the mod developer if the issue persists.");
				msg.show();
				return;
			}

			File source;
			if(inRoot)
				source = root;
			else
				source = new File(root + File.separator + mod);
			File destination = new File(gamePath + File.separator + "mods" + File.separator + mod);
			if(destination.exists()) { // Wipe out any previous installation to avoid instances of failed
				try {
					FileUtils.delete(destination, FileUtils.RECURSIVE);
				} catch (IOException e) {
					Alert msg = new Alert(Alert.AlertType.ERROR, "Could not delete previous mod installation!\n\nA possible workaround might be to delete the mod folder yourself, then try again.");
					msg.show();
					return;
				}
			}
			if(destination.exists()) { // We failed to delete the folder, but didn't throw an exception.
				Alert msg = new Alert(Alert.AlertType.ERROR, "Could not delete previous mod installation!\n\nA possible workaround might be to delete the mod folder yourself, then try again.");
				msg.show();
				return;
			}

			destination.mkdirs();
			Files.walkFileTree(source.getAbsoluteFile().toPath(), new CopyFileVisitor(destination.getAbsoluteFile().toPath()));
			Alert msg = new Alert(Alert.AlertType.INFORMATION, "Mod successfully installed!");
			msg.show();
		} catch (Exception e) {
			Alert msg = new Alert(Alert.AlertType.ERROR, "Encountered an exception: " + e.toString());
			e.printStackTrace();
			msg.show();
		}
	}

	private void getBranches() throws GitAPIException {
		repo.fetch().call();
		List<Ref> tags = repo.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
		tags.addAll(repo.tagList().call());

		List<String> tagNames = new ArrayList<>();
		branches.clear();
		for (Ref tag : tags) {
			tagNames.add(tag.getName().replaceFirst("refs/remotes/origin/|refs/tags/", ""));
			branches.put(tag.getName().replaceFirst("refs/remotes/origin/|refs/tags/", ""), tag);
		}

		Platform.runLater(() -> {
			branchList.getItems().clear();
			branchList.getItems().addAll(tagNames);
		});
	}

	private void getDescription() throws GitAPIException, IOException {
		Repository repository = repo.getRepository();
		ObjectId treeId = repository.resolve("master^{tree}");
		TreeWalk walker = Utils.generateReadmeWalker(repository, treeId);
		ObjectId readmeId = walker.getObjectId(0);
		ObjectLoader readmeLoader = repo.getRepository().open(readmeId);

		try (
				InputStream readmeStream = readmeLoader.openStream();
				BufferedReader readmeReader = new BufferedReader(new InputStreamReader(readmeStream));
		)
		{
			modInfo.setText(readmeReader.lines().collect(Collectors.joining("\n")));
		}
		catch(NullPointerException|IOException e) {
			modInfo.setText("No description could be found for this mod.");
		}

		Platform.runLater(() -> branchInfo.setText("No branches selected"));
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
		final double wndwWidth = 300.0d;
		Label updateLabel = new Label("Loading repository...");
		updateLabel.setPrefWidth(wndwWidth);
		ProgressBar progress = new ProgressBar();
		progress.setPrefWidth(wndwWidth);

		VBox updatePane = new VBox();
		updatePane.setPadding(new Insets(10));
		updatePane.setSpacing(5.0d);
		updatePane.getChildren().addAll(updateLabel, progress);

		Stage taskUpdateStage = new Stage(StageStyle.UTILITY);
		taskUpdateStage.initModality(Modality.APPLICATION_MODAL);
		taskUpdateStage.setScene(new Scene(updatePane));
		taskUpdateStage.show();

		Task updateTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				try {
					updateMessage("Loading repository...");
					repo = Git.open(dir);

					updateMessage("Fetching branch list...");
					getBranches();

					updateMessage("Getting mod description...");
					getDescription();
				}
				catch (RepositoryNotFoundException e) {
					e.printStackTrace();
					Platform.runLater(() -> {
						Alert msg = new Alert(Alert.AlertType.ERROR, "Not a valid repository!");
						msg.show();
					});
					throw e;
				}
				catch (Exception e) {
					e.printStackTrace();
					Platform.runLater(() -> {
						Alert msg = new Alert(Alert.AlertType.ERROR, "Encountered an exception: " + e.toString());
						msg.show();
					});
					throw e;
				}
				return null;
			}
		};

		updateTask.setOnSucceeded(t -> {
			taskUpdateStage.close();

			String url = repo.getRepository().getConfig().getString("remote", "origin", "url");
			if(url == null)
				url = "a local repository at " + dir.getAbsolutePath();

			urlLabel.setText("Connected to " + url);
		});
		updateTask.setOnFailed(t -> taskUpdateStage.close());
		progress.progressProperty().bind(updateTask.progressProperty());
		updateLabel.textProperty().bind(updateTask.messageProperty());

		taskUpdateStage.show();
		new Thread(updateTask).start();
	}

	@FXML private void close(ActionEvent actionEvent) {
		Alert dialog = new Alert(Alert.AlertType.CONFIRMATION, "Quit SR2 Mod Manager?");
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
		Alert dialog = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this repository from your computer? You will not be able to access it again until you redownload it.");
		ObservableList<ButtonType> dlgButtons = dialog.getDialogPane().getButtonTypes();
		dlgButtons.clear();
		dlgButtons.add(ButtonType.YES);
		dlgButtons.add(ButtonType.NO);
		dialog.setHeaderText("Delete Repository?");

		dialog.showAndWait()
				.filter(response -> response == ButtonType.YES)
				.ifPresent(response -> {
						File root = repo.getRepository().getWorkTree();
						repo.close();
						Utils.deleteFolder(root);

						if(root.exists()) {
							Alert msg = new Alert(Alert.AlertType.ERROR, "Could not delete repository!\n\nFor some reason, the repository was not deleted. You may have to delete it yourself.");
							msg.show();
						}
						else {
							branches.clear();
							branchList.getItems().clear();
							modInfo.setText("");
							branchInfo.setText("");
						}
				});
	}

	@FXML private void about(ActionEvent actionEvent) {
		Alert msg = new Alert(Alert.AlertType.INFORMATION,
				"SR2 Mod Manager by Dalo Lorn\n" +
						"\n" +
						"Version: 1.0.0 Beta 1\n\n" +
						"GitHub: https://github.com/DaloLorn/SR2ModManager\n" +
						"Discord: https://discord.gg/sUJKJDc\n" +
						"Patreon: https://patreon.com/rising_stars_sr2");
		msg.setHeaderText("About SR2 Mod Manager");
		msg.show();
	}
}
