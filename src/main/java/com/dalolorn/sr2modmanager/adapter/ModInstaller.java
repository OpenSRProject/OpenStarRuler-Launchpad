package com.dalolorn.sr2modmanager.adapter;

import com.dalolorn.sr2modmanager.model.Metadata;
import com.dalolorn.sr2modmanager.model.Modinfo;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModInstaller {
	public static final String NO_BRANCH_DESC = "No description could be found for this branch.";
	public static final String ENCOUNTERED_AN_EXCEPTION = "Encountered an exception: ";
	public static final String PROTOCOL_GIT = "git://";
	public static final String PROTOCOL_HTTPS = "https://";
	public static final String NO_MOD_DESC = "No description could be found for this mod.";

	private static Git repo;

	private static Ref currentBranch;
	@NotNull private static final Map<String, Ref> branches = new HashMap<>();

	public interface TextHandler {
		void handle(@NotNull String text);
	}

	public static boolean hasRepo() {
		return repo != null;
	}

	public static boolean hasBranch() {
		return currentBranch != null;
	}

	@NotNull
	public static String setActiveBranch(@NotNull String branchName) {
		currentBranch = branches.get(branchName);

		ObjectLoader descriptionLoader;
		try {
			var repository = repo.getRepository();
			var treeId = repository.resolve(currentBranch.getName() + "^{tree}");
			var walker = Utils.generateBranchDescWalker(repository, treeId);
			var descriptionId = walker.getObjectId(0);
			descriptionLoader = repository.open(descriptionId);
		} catch (Exception e) {
			e.printStackTrace();
			return NO_BRANCH_DESC;
		}

		var json = Utils.readGitFile(descriptionLoader);
		if(json != null) {
			try {
				Map<String, String> descriptions = new Gson().fromJson(json, new TypeToken<HashMap<String, String>>() {}.getType());
				if (descriptions != null && !descriptions.isEmpty()) {
					return descriptions.getOrDefault(branchName, NO_BRANCH_DESC);
				} else {
					return NO_BRANCH_DESC;
				}
			} catch (JsonSyntaxException e) {
				return json;
			}
		}
		else return NO_BRANCH_DESC;
	}

	@Nullable
	public static String connectToRepository(
			@NotNull String url,
			@Nullable TextHandler progressHandler,
			@Nullable TextHandler errorHandler
	) {
		if(progressHandler == null) progressHandler = text -> {}; // Let's not get bogged down in a zillion null checks, eh?

		try {
			progressHandler.handle("Parsing URL...");
			if (url.equals("")) {
				if (errorHandler != null) {
					errorHandler.handle("URL field is empty! Cannot connect to the cold void of space.\n\nPlease enter the URL of the Git repository you want to connect to.");
				}
				return null;
			}
			url = parseRepoURL(url);
			File localRoot = getLocalRoot(url);

			progressHandler.handle("Loading repository...");
			Git tmp = cloneRepository(localRoot, url);
			if(repo != null) repo.close();
			repo = tmp;
			return url;
		} catch (Exception e) {
			if(errorHandler != null) {
				if (e.getCause() instanceof NotSupportedException && e.getCause().getMessage().startsWith("URI not supported: "))
					errorHandler.handle("Not a valid Git URL!\n\nThe URL provided isn't a valid Git URL.\n\nPlease make sure you entered the correct URL, and contact the SR2MM developers if the problem persists.");
				else
					errorHandler.handle(ENCOUNTERED_AN_EXCEPTION + e);
			}
			e.printStackTrace();
			return null;
		}
	}

	@NotNull
	private static Git cloneRepository(@NotNull File localRoot, @NotNull String url) throws GitAPIException {
		try {
			return Git.open(localRoot);
		} catch (IOException e) {
			CloneCommand cmd = Git.cloneRepository()
					.setDirectory(localRoot);
			try {
				return cmd.setURI(url)
						.call();
			} catch (Exception e2) {
				// Apparently the Git protocol isn't yet supported by all Git servers.
				// So if the clone failed, it might not mean an HTTPS clone would do the same.
				if (url.startsWith(PROTOCOL_GIT)) {
					return cmd.setURI(url.replace(PROTOCOL_GIT, PROTOCOL_HTTPS))
							.call();
				}
				else throw e2;
			}
		}
	}

	@Nullable
	public static String openRepository(
			@NotNull File dir,
			@NotNull TextHandler errorHandler
	) {
		try {
			var tmp = Git.open(dir);

			if(repo != null) repo.close();
			repo = tmp;

			var url = repo.getRepository().getConfig().getString("remote", "origin", "url");
			if(url == null)
				url = "a local repository at " + dir.getAbsolutePath();
			return url;
		} catch (RepositoryNotFoundException e) {
			e.printStackTrace();
			errorHandler.handle("Not a valid repository!");
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			errorHandler.handle(ENCOUNTERED_AN_EXCEPTION + e);
			return null;
		}
	}

	@Nullable
	public static List<String> getBranches(@Nullable TextHandler errorHandler) {
		try {
			repo.fetch().call();
			List<Ref> tags = repo.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
			tags.addAll(repo.tagList().call());

			List<String> tagNames = new ArrayList<>();
			branches.clear();
			for (Ref tag : tags) {
				String tagName = tag.getName().replaceFirst("refs/remotes/origin/|refs/tags/", "");
				tagNames.add(tagName);
				branches.put(tagName, tag);
			}

			return tagNames;
		} catch (Exception e) {
			if(errorHandler != null)
				errorHandler.handle(ENCOUNTERED_AN_EXCEPTION + e);
			e.printStackTrace();
			return null;
		}
	}

	@Nullable
	public static String getDescription(@Nullable TextHandler errorHandler) {
		try {
			var repository = repo.getRepository();
			var treeId = repository.resolve("master^{tree}");
			var walker = Utils.generateReadmeWalker(repository, treeId);
			var readmeId = walker.getObjectId(0);
			var readmeLoader = repository.open(readmeId);

			var description = Utils.readGitFile(readmeLoader);
			if (description != null)
				return description;
			else return NO_MOD_DESC;
		} catch (Exception e) {
			if(errorHandler != null)
				errorHandler.handle(ENCOUNTERED_AN_EXCEPTION + e);
			e.printStackTrace();
			return null;
		}
	}

	@NotNull
	private static String parseRepoURL(@NotNull String url) {
		if (!url.startsWith("http") && !url.startsWith(PROTOCOL_GIT))
			url = PROTOCOL_GIT + url;
		if (!url.endsWith(".git"))
			url += ".git";
		return url;
	}

	@NotNull
	private static File getLocalRoot(@NotNull String url) throws IOException {
		String[] path = url.split("/");
		String repoName = path[path.length - 2] + "_" + path[path.length - 1];
		repoName = repoName.substring(0, repoName.length() - 4);
		var localRoot = new File("repositories" + File.separator + repoName);
		if (!localRoot.exists() || !localRoot.isDirectory())
			localRoot = Files.createDirectories(localRoot.toPath()).toFile();
		return localRoot;
	}

	private static void installModImpl(
			@NotNull Git repo,
			@NotNull TextHandler warningHandler,
			@NotNull TextHandler progressHandler,
			@Nullable TextHandler infoHandler,
			@NotNull TextHandler errorHandler
	) throws IOException {
		File root = repo.getRepository().getWorkTree();

		progressHandler.handle("Parsing installation instructions...");
		var metadata = new File(root.getAbsolutePath() + File.separator + "metadata.json");
		if(metadata.exists()) {
			try (var reader = new FileReader(metadata)) {
				Metadata meta = new Gson().fromJson(reader, Metadata.class);

				if(meta.dependencies != null)
					for (Metadata.Dependency dependency : meta.dependencies)
						if(!installDependency(dependency, progressHandler, warningHandler))
							warningHandler.handle("WARNING: Failed to install dependency " + dependency.name + "!\n\nThe mod may behave erratically or fail to start. Please try to install the dependency manually, or contact the mod (and/or dependency) developers.");
			} catch (Exception e) {
				warningHandler.handle("WARNING: Unable to read installation instructions!\n\nThis mod's repository contains an additional metadata file (metadata.json) with additional information required for a successful installation, such as the locations of any prerequisite mods.\nPlease review the metadata file and take the necessary actions, or contact the mod developer.");
				e.printStackTrace();
				Desktop.getDesktop().open(metadata);
			}
		}

		progressHandler.handle("Finding modinfo...");
		Modinfo modinfo = findModinfo(root, warningHandler);
		if(modinfo == null) {
			errorHandler.handle("Cannot find modinfo.txt!\n\nThis repository does not appear to contain a valid Star Ruler 2 mod.\nPlease make sure that you have connected to the right repository, and contact the mod developer if the issue persists.");
			return;
		}

		progressHandler.handle("Found modinfo, preparing installation directory...");
		File source;
		if(modinfo.inRoot)
			source = root;
		else
			source = new File(root + File.separator + modinfo.folderName);
		var destination = findModInstallDir(modinfo);
		// Wipe out any previous installation, just to be sure.
		Utils.deleteFolder(destination);
		if(destination.exists()) { // We failed to delete the previous installation.
			errorHandler.handle("Could not delete previous mod installation!\n\nA possible workaround might be to delete the mod folder yourself, then try again.");
			return;
		}

		progressHandler.handle("Copying mod files from repository...");
		destination.mkdirs();
		Files.walkFileTree(source.getAbsoluteFile().toPath(), new CopyFileVisitor(destination.getAbsoluteFile().toPath()));
		if(infoHandler != null)
			infoHandler.handle("Mod successfully installed!");
	}

	private static Modinfo findModinfo(
			@NotNull File root,
			@Nullable TextHandler warningHandler
	) throws IOException {
		var inRoot = false;
		String mod;
		var finder = new SingleFinder("modinfo.txt");
		Files.walkFileTree(root.getAbsoluteFile().toPath(), finder);
		if(finder.getResult() != null) {
			mod = finder.getResult().getParent().getFileName().toString();
			if(mod.equalsIgnoreCase(root.toPath().getFileName().toString())) {
				inRoot = true; // The modinfo is in the repository root, so we can't discard metadata.
				if(warningHandler != null)
					warningHandler.handle("WARNING: Unable to discard repository metadata!\n\nTo improve loading times, it is recommended that you delete the installed mod's .git folder once installation is completed.");
			}
			return new Modinfo(inRoot, mod);
		}
		else {
			return null;
		}
	}

	private static File findModInstallDir(
			@NotNull Modinfo modinfo
	) {

		return new File(Settings.getInstance().getModsFolder() + File.separator + modinfo.folderName);
	}

	private static boolean installDependency(
			@NotNull Metadata.Dependency dependency,
			@NotNull TextHandler progressHandler,
			@NotNull TextHandler warningHandler
	) {
		try {
			progressHandler.handle("Parsing URL for dependency \"" + dependency.name + "\"...");
			if (dependency.repository.equals("")) {
				warningHandler.handle(String.format("Failed to install dependency \"%s\": Dependency metadata doesn't specify a URL. The author may be playing a prank on his users.", dependency.name));
				return false;
			}
			String url = parseRepoURL(dependency.repository);
			File localRoot = getLocalRoot(url);

			progressHandler.handle("Loading dependency \"" + dependency.name + "\"...");
			Git depRepo = cloneRepository(localRoot, url);

			depRepo.checkout()
					.setName(dependency.branch)
					.setCreateBranch(depRepo.getRepository().resolve("refs/heads/" + dependency.branch) == null)
					.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
					.setStartPoint("refs/remotes/origin/" + dependency.branch)
					.call();

			depRepo.pull().call();

			installModImpl(
					depRepo,
					warning -> warningHandler.handle(String.format("Encountered warning while installing dependency \"%s\": %s", dependency.name, warning)),
					progress -> {
						if(!progress.startsWith("Installing dependency"))
							progress = String.format("Installing dependency \"%s\": %s", dependency.name, progress);
						progressHandler.handle(progress);
					},
					null,
					error -> warningHandler.handle(String.format("Failed to install dependency \"%s\": %s", dependency.name, error))
			);
			return true;
		} catch (Exception e) {
			if (e.getCause() instanceof NotSupportedException && e.getCause().getMessage().startsWith("URI not supported: "))
				warningHandler.handle(String.format("Failed to install dependency \"%s\": Not a valid Git URL!%n%nThe URL in the dependency metadata is not a valid Git URL.%n%nPlease contact the mod author and/or SR2MM developers.", dependency.name));
			else
				warningHandler.handle(String.format("Failed to install dependency \"%s\": %s", dependency.name, e));
			e.printStackTrace();
			return false;
		}
	}

	public static void installMod(
			@Nullable TextHandler warningHandler,
			@Nullable TextHandler progressHandler,
			@Nullable TextHandler infoHandler,
			@Nullable TextHandler errorHandler
	) {
		if(warningHandler == null) warningHandler = text -> {};
		if(progressHandler == null) progressHandler = text -> {};
		if(errorHandler == null) errorHandler = text -> {};

		try {
			progressHandler.handle("Checking out target branch or tag...");
			boolean isTag = currentBranch.getName().startsWith("refs/tags/");
			boolean createBranch = !isTag && repo.getRepository().resolve(currentBranch.getName().replaceFirst("refs/remotes/origin", "refs/heads")) == null;

			repo.checkout()
					.setName(currentBranch.getName().replaceFirst("refs/remotes/origin/", ""))
					.setCreateBranch(createBranch)
					.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
					.setStartPoint(currentBranch.getName())
					.call();

			if(!isTag) {
				progressHandler.handle("Pulling upstream changes...");
				repo.pull().call();
			}

			installModImpl(repo, warningHandler, progressHandler, infoHandler, errorHandler);
		} catch (RefNotAdvertisedException e) {
			errorHandler.handle("This branch or tag is no longer visible. It may have been deleted from the origin repository, or it may have been hidden somehow.\n\nThis may often be the case for branches used in beta testing or miscellaneous development; try another one, or contact the mod developers.");
			e.printStackTrace();
		} catch (Exception e) {
			errorHandler.handle(ENCOUNTERED_AN_EXCEPTION + e);
			e.printStackTrace();
		}
	}

	public static boolean deleteRepository(@NotNull TextHandler errorHandler) {
		File root = repo.getRepository().getWorkTree();
		repo.close();
		repo = null;
		Utils.deleteFolder(root);

		if(root.exists()) {
			errorHandler.handle("Could not delete repository!\n\nFor some reason, the repository was not deleted. You may have to delete it yourself.");
			return false;
		}
		else {
			branches.clear();
			return true;
		}
	}

	public static boolean uninstallMod(@NotNull TextHandler errorHandler) {
		File root = repo.getRepository().getWorkTree();
		Modinfo modinfo = null;
		try {
			modinfo = findModinfo(root, null);
		} catch (Exception e) {
			errorHandler.handle("Encountered an exception trying to find the modinfo file!\n\nFor some reason, the mod folder couldn't be detected. You may have to delete it yourself.");
		}
		if(modinfo == null)
			return false;
		File installDir = findModInstallDir(modinfo);
		Utils.deleteFolder(installDir);

		if(installDir.exists()) {
			errorHandler.handle("Could not delete mod folder!\n\nFor some reason, the mod folder couldn't be deleted. You may have to delete it yourself.");
			return false;
		}
		else {
			return true;
		}
	}
}
