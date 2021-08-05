package com.dalolorn.sr2modmanager.adapter;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Locale;
import java.util.stream.Collectors;

/** Utility class containing a number of helper functions. */
public class Utils {
	private Utils() {}

	/** Iterates through a list of likely variations on the filename 'branch-description.txt', which is the expected branch description.
	 *
	 * If none exists, falls back to the readme file. */
	static TreeWalk generateBranchDescWalker(Repository repo, ObjectId tree) throws FileNotFoundException {
		TreeWalk result = null;
		try {
			result = TreeWalk.forPath(repo, "branch-descriptions.json", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "Branch-Descriptions.json", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "BRANCH-DESCRIPTIONS.json", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "branch-description.txt", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "Branch-Description.txt", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "BRANCH-DESCRIPTION.txt", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "BRANCH-DESCRIPTIONS.JSON", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "Branch-Descriptions.JSON", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "branch-descriptions.JSON", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "BRANCH-DESCRIPTION.TXT", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "Branch-Description.TXT", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "branch-description.TXT", tree);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (result == null) {
			System.err.println("Could not find branch-description.txt, attempting to pull README.md instead...");
			result = generateReadmeWalker(repo, tree);
		}

		return result;
	}

	public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase(Locale.ROOT).trim().startsWith("win");

	/** Iterates through a list of likely readme names. I'd use case-insensitive filtering, but I don't fancy figuring out how to write a case-insensitive version of PathFilter. */
	static TreeWalk generateReadmeWalker(Repository repo, ObjectId tree) throws FileNotFoundException {
		TreeWalk result = null;
		try {
			result = TreeWalk.forPath(repo, "README.md", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "README.MD", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "Readme.md", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "readme.md", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "Readme.MD", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "readme.MD", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "readme.txt", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "Readme.txt", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "README.txt", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "README.TXT", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "Readme.TXT", tree);
			if (result == null)
				result = TreeWalk.forPath(repo, "readme.TXT", tree);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (result == null)
			throw new FileNotFoundException("Could not find README.md!");

		return result;
	}

	/** Deletes a folder and all of its contents from the hard drive. */
	static void deleteFolder(@NotNull File folder) {
		if(!folder.exists())
			return; // The folder's not null, but that doesn't mean it exists.

		File[] files = folder.listFiles();
		if(files!=null) { //some JVMs return null for empty dirs
			for(File f: files) {
				if(f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}

	static String readGitFile(@NotNull ObjectLoader fileLoader) {
		try (
				var fileStream = fileLoader.openStream();
				var fileReader = new BufferedReader(new InputStreamReader(fileStream))
		) {
			return fileReader.lines().collect(Collectors.joining("\n"));
		} catch (NullPointerException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}