package com.dalolorn.sr2modmanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.util.FileUtils;

public class Main {
    private static final String DEFAULT_REMOTE = "https://github.com/DaloLorn/Rising-Stars.git";
    private static final String DEFAULT_BRANCH = "WorkshopBuild";

    public static void main(String[] args) throws IOException, GitAPIException {
        // Prepare a temporary folder to download the mod into.
        File localPath = File.createTempFile("SR2ModManager", "");
        if(!localPath.delete()) {
            throw new IOException("Could not delete temporary file " + localPath);
        }


	    // Set up all relevant paths.
        String url = DEFAULT_REMOTE;
        String branch = DEFAULT_BRANCH;
        if(args.length == 2) {
	        	url = args[0];
	        	branch = args[1];
        }
        else {
        	Scanner input = new Scanner(System.in);
        	System.out.print("Please input the URL of the mod's Git repository, or press Enter to download Rising Stars: ");
        	String data = input.nextLine().trim();
        	if(!data.equalsIgnoreCase(""))
        		url = data;

        	System.out.print("Please input the name of the branch you wish to download from, or press Enter to use the 'WorkshopBuild' branch: ");
        	data = input.nextLine().trim();
	        if(!data.equalsIgnoreCase(""))
	        	branch = data;
	        System.out.println();
        }

        // Clone the repo.
        System.out.println("Cloning from " + url + ", branch " + branch + ", to " + localPath);
        Git repo = Git.cloneRepository()
	            .setURI(url)
		        .setBranch(branch)
				.setDirectory(localPath)
				.call();

	    // Find the folder containing the modinfo file.
	    boolean inRoot = false;
	    String mod = null;
	    Finder finder = new Finder("modinfo.txt");
	    Files.walkFileTree(localPath.getAbsoluteFile().toPath(), finder);
	    if(finder.getResult() != null) {
	    	mod = finder.getResult().getParent().getFileName().toString();
	    	if(mod.equalsIgnoreCase(localPath.toPath().getFileName().toString())) {
	    		inRoot = true; // The modinfo is in the repository root, so we can't discard metadata.
			    String[] path;
	    		path = url.split("/");
	    		mod = path[path.length-1];
	    		mod = mod.replace(".git", "");
	    		System.out.println("WARNING: Unable to discard repository metadata!");
		    }
	    }
		else {
	    	System.out.println("ERROR: Could not find modinfo.txt!");
	    	System.exit(-1);
	    }

	    System.out.println("Retrieved repository for mod: " + mod);
	    System.out.println("Branch retrieved: " + repo.getRepository().getBranch());
	    repo.close();

        File source;
        if(inRoot)
	        source = localPath;
	    else
        	source = new File(localPath + File.separator + mod);
        File destination = new File(".." + File.separator + "mods" + File.separator + mod);
        if(destination.exists()) { // Wipe out any previous installation to avoid instances of failed
	        try {
		        FileUtils.delete(destination, FileUtils.RECURSIVE);
	        } catch (IOException e) {
		        System.out.println("ERROR: Could not delete previous mod installation!");
		        System.exit(-1);
	        }
        }
        if(destination.exists()) { // We failed to delete the folder, but didn't throw an exception.
	        System.out.println("ERROR: Could not delete previous mod installation!");
	        System.exit(-1);
        }

        destination.mkdirs();
	    Files.move(source.getAbsoluteFile().toPath(), destination.getAbsoluteFile().toPath(), StandardCopyOption.REPLACE_EXISTING);

	    // We want to discard the temporary folder and any of its remaining contents once the download is complete.

	    if(!inRoot) {
		    try {
			    FileUtils.delete(localPath, FileUtils.RECURSIVE);
		    } catch (IOException e) {
			    System.out.println("WARNING: Could not delete temporary folders!");
		    }
	    }
    }
}