package com.dalolorn.sr2modmanager.adapter;

import com.dalolorn.sr2modmanager.model.Metadata;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.RefNotAdvertisedException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FileUtils;

import java.util.ArrayList;
import java.util.List;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RepositoryManager {
	public static Git repo;

	public static Ref currentBranch;
	public static Map<String, Ref> branches = new HashMap<>();

	public interface TextHandler {
		void handle(String text);
	}

	public static String setActiveBranch(String branchName) {
		currentBranch = branches.get(branchName);

		ObjectLoader descriptionLoader;
		try {
			Repository repository = repo.getRepository();
			ObjectId treeId = repository.resolve(currentBranch.getName() + "^{tree}");
			TreeWalk walker = Utils.generateBranchDescWalker(repository, treeId);
			ObjectId descriptionId = walker.getObjectId(0);
			descriptionLoader = repository.open(descriptionId);
		} catch (Exception e) {
			e.printStackTrace();
			return "No description could be found for this branch.";
		}
		try (
				InputStream descriptionStream = descriptionLoader.openStream();
				BufferedReader descriptionReader = new BufferedReader(new InputStreamReader(descriptionStream))
		) {
			String json = descriptionReader.lines().collect(Collectors.joining("\n"));
			try {
				Map<String, String> descriptions = new Gson().fromJson(json, new TypeToken<HashMap<String, String>>(){}.getType());
				if(descriptions != null && !descriptions.isEmpty()) {
					return descriptions.getOrDefault(
							branchName,
							"No description could be found for this branch."
					);
				}
				else {
					return "No description could be found for this branch.";
				}
			} catch (JsonSyntaxException e) {
				return json;
			}
		} catch (NullPointerException | IOException e) {
			return "No description could be found for this branch.";
		}
	}

	public static String connectToRepository(String url, TextHandler progressHandler, TextHandler errorHandler) {
		if(progressHandler == null) progressHandler = text -> {}; // Let's not get bogged down in a zillion null checks, eh?

		try {
			progressHandler.handle("Parsing URL...");
			url = parseRepoURL(url);
			File localRoot = getLocalRoot(url);

			progressHandler.handle("Loading repository...");
			Git tmp;
			try {
				tmp = Git.open(localRoot);
			} catch (IOException e) {
				tmp = Git.cloneRepository()
						.setURI(url)
						.setDirectory(localRoot)
						.call();
			}
			if(tmp != null) {
				if(repo != null) repo.close();
				repo = tmp;
			}
			return url;
		} catch (Exception e) {
			if(errorHandler != null)
				errorHandler.handle("Encountered an exception: " + e.toString());
			e.printStackTrace();
			return null;
		}
	}

	public static String openRepository(File dir, TextHandler errorHandler) {
		try {
			Git tmp = Git.open(dir);
			if(tmp != null) {
				if(repo != null) repo.close();
				repo = tmp;
			}
			String url = repo.getRepository().getConfig().getString("remote", "origin", "url");
			if(url == null)
				url = "a local repository at " + dir.getAbsolutePath();
			return url;
		} catch (RepositoryNotFoundException e) {
			e.printStackTrace();
			errorHandler.handle("Not a valid repository!");
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			errorHandler.handle("Encountered an exception: " + e.toString());
			return null;
		}
	}

	public static List<String> getBranches(TextHandler errorHandler) {
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
				errorHandler.handle("Encountered an exception: " + e.toString());
			e.printStackTrace();
			return null;
		}
	}

	public static String getDescription(TextHandler errorHandler) {
		try {
			Repository repository = repo.getRepository();
			ObjectId treeId = repository.resolve("master^{tree}");
			TreeWalk walker = Utils.generateReadmeWalker(repository, treeId);
			ObjectId readmeId = walker.getObjectId(0);
			ObjectLoader readmeLoader = repository.open(readmeId);

			try (
					InputStream readmeStream = readmeLoader.openStream();
					BufferedReader readmeReader = new BufferedReader(new InputStreamReader(readmeStream))
			) {
				return readmeReader.lines().collect(Collectors.joining("\n"));
			} catch (NullPointerException | IOException e) {
				return "No description could be found for this mod.";
			}
		} catch (Exception e) {
			if(errorHandler != null)
				errorHandler.handle("Encountered an exception: " + e.toString());
			e.printStackTrace();
			return null;
		}
	}

	private static String parseRepoURL(String url) {
		if (!url.startsWith("http"))
			url = "https://" + url;
		if (!url.endsWith(".git"))
			url += ".git";
		return url;
	}

	private static File getLocalRoot(String url) throws IOException {
		String[] path = url.split("/");
		String repoName = path[path.length - 2] + "_" + path[path.length - 1];
		repoName = repoName.substring(0, repoName.length() - 4);
		File localRoot = new File("repositories" + File.separator + repoName);
		if (!localRoot.exists() || !localRoot.isDirectory())
			localRoot = Files.createDirectories(localRoot.toPath()).toFile();
		return localRoot;
	}

	private static void installModImpl(Git repo, TextHandler warningHandler, TextHandler progressHandler, TextHandler infoHandler, TextHandler errorHandler) throws IOException {
		File root = repo.getRepository().getWorkTree();

		progressHandler.handle("Parsing installation instructions...");
		File metadata = new File(root.getAbsolutePath() + File.separator + "metadata.json");
		if(metadata.exists()) {
			try (FileReader reader = new FileReader(metadata)) {
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
		boolean inRoot = false;
		String mod;
		SingleFinder finder = new SingleFinder("modinfo.txt");
		Files.walkFileTree(root.getAbsoluteFile().toPath(), finder);
		if(finder.getResult() != null) {
			mod = finder.getResult().getParent().getFileName().toString();
			if(mod.equalsIgnoreCase(root.toPath().getFileName().toString())) {
				inRoot = true; // The modinfo is in the repository root, so we can't discard metadata.
				warningHandler.handle("WARNING: Unable to discard repository metadata!\n\nTo improve loading times, it is recommended that you delete the installed mod's .git folder once installation is completed.");
			}
		}
		else {
			errorHandler.handle("Cannot find modinfo.txt!\n\nThis repository does not appear to contain a valid Star Ruler 2 mod.\nPlease make sure that you have connected to the right repository, and contact the mod developer if the issue persists.");
			return;
		}

		progressHandler.handle("Found modinfo, preparing installation directory...");
		File source;
		if(inRoot)
			source = root;
		else
			source = new File(root + File.separator + mod);
		File destination = new File(Settings.getInstance().gamePath + File.separator + "mods" + File.separator + mod);
		if(destination.exists()) { // Wipe out any previous installation, just to be sure.
			try {
				FileUtils.delete(destination, FileUtils.RECURSIVE);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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

	private static boolean installDependency(Metadata.Dependency dependency, TextHandler progressHandler, TextHandler warningHandler) {
		try {
			progressHandler.handle("Parsing URL for dependency \"" + dependency.name + "\"...");
			String url = parseRepoURL(dependency.repository);
			File localRoot = getLocalRoot(url);

			progressHandler.handle("Loading dependency \"" + dependency.name + "\"...");
			Git depRepo;
			try {
				depRepo = Git.open(localRoot);
			} catch (IOException e) {
				depRepo = Git.cloneRepository()
						.setURI(url)
						.setDirectory(localRoot)
						.call();
			}
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
			warningHandler.handle(String.format("Failed to install dependency \"%s\": %s", dependency.name, e.toString()));
			e.printStackTrace();
			return false;
		}
	}

	public static void installMod(TextHandler warningHandler, TextHandler progressHandler, TextHandler infoHandler, TextHandler errorHandler) {
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
			errorHandler.handle("Encountered an exception: " + e.toString());
			e.printStackTrace();
		}
	}

	public static boolean deleteRepository(TextHandler errorHandler) {
		File root = repo.getRepository().getWorkTree();
		repo.close();
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
}
