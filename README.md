# OpenSR Launchpad

A small, cross-platform tool capable of installing, updating and uninstalling Star Ruler 2 mods uploaded to online Git repositories, such as the ones hosted by GitHub, GitLab, or Bitbucket, as well as installing and updating the OpenSR distribution of Star Ruler 2.

## Table of Contents

1) Installation
2) Basic Use
3) Advanced Techniques
4) Modder's Guide
5) Troubleshooting FAQ
6) Contact

## Installation

In order to run this application, you must first have a computer with all of the following:

- Java Runtime Environment 11 or newer (also known as Java 11). This is not required if you install the standalone version of the Launchpad, but if possible, it is recommended that you use the standard version.
- An Internet connection

The current .zip download contains the following file structure:

- OpenStarRuler-Launchpad
    - lib (STANDARD VERSION ONLY)
	    - *A folder containing a variety of .jar files required by the launchpad, including the manager itself. This is not present in the standalone version of the Launchpad.*
    - jre (STANDALONE VERSION ONLY)
		- *A folder containing the Launchpad and its runtime environment. This is not present in the standard version of the Launchpad.*
	- OSRLaunchpad.bat
	- OSRLaunchpad
		- *A batch file which will start the launchpad, as well as its Linux equivalent.*
	- history.json
		- *This file initially contains a list of 5 suggested repositories. As you connect to other repositories, the suggested repos will be gradually pushed out of the list to store the last 5 repos you opened.*
	- README.md
		- *You are here*

To install the Launchpad, extract the `OpenStarRuler-Launchpad` folder wherever you like. It is recommended, but not mandatory, that you install the Launchpad wherever your Star Ruler 2 installation is (or will be).

## Basic Use

On Windows, just run `OSRLaunchpad.bat` to run the application.

On Linux, the equivalent action is to run `OSRLaunchpad`.

### First Run

When you first start the Launchpad, you will be prompted to navigate to your Star Ruler 2 installation. If you do not have one, or if you do not want to install OpenSR on top of your existing installation, you should instead select an empty folder.

After this is done, you will be asked if you want to install the latest stable version of OpenSR now. This is recommended, but if you choose not to do it, you may do so later at any time.

### Installing/Updating OpenSR

Installing/updating OpenSR is a fairly straightforward process. Start by switching to the "Manage OpenSR" tab.

If you have not yet selected an installation directory, or if you want to install/update to a different directory, you may do so now via the "Set SR2 Path" button.

Select your preferred version of OpenSR from the list in the upper left section of the window, then press the "Update Game" button. If you do not select any version, the Launchpad will default to installing the latest stable version.

If this is your first time installing OpenSR to this directory, you may need to wait a while for the data repository to be downloaded. Download sizes for the OpenSR-Data repository are estimated at around 500MB, and the complete game is a little under 900MB. Be advised that the Launchpad needs to maintain both the data repository and the game itself, so the total size of the game should be around 1.4-1.5GB. (Mods - especially shipset mods - and user data may inflate this figure considerably. To avoid running out of space, it is recommended that you reserve at least 25GB of disk space for OpenSR. For reference, at the time of writing, the author of this readme loosely estimated that he had ~6.5GB of Workshop mods, ~600MB of user data, and ~2.9GB of non-Steam mods on his machine. In terms of raw size, this probably encompasses a large proportion of all Star Ruler 2 mods ever released to the public, but that's no reason not to future-proof your game with a generous dose of padding.)

After the download is complete, it may take a little longer to check out the correct revision from the repository. This shouldn't take long, and after it is finished, you will be able to run the game from any Launchpad tab, via the "Play" button.

### Installing/Updating Mods

To install/update a mod, you must first connect to its public Git repository. A registry of Launchpad-installable mods and their repos is currently being maintained in the #mods-and-tools channel of the OpenSR Discord server, located at https://discord.gg/sUJKJDc. Start by switching to the "Install Mods" tab.

To connect to a repository, enter its URL as if you were trying to access it via a browser, then press 'Connect'. If the protocol is omitted, the Launchpad will default to `https://` - similarly, if the `.git` file extension is missing, the Launchpad will automatically append it to the URL. (Consequently, a link such as `github.com/OpenSRProject/OpenStarRuler-Modpack` would be a perfectly valid way of accessing the repository containing the OpenSR Modpack.)

You can also open a previously downloaded repository by using 'File->Open' and selecting the desired repository folder. (For instance, the OpenSR Modpack would be stored in `OpenSRProject_OpenStarRuler-Modpack`.)

Once a repository has been opened, the branch list in the upper left section of the window will fill up with branch and tag names. Click on one of these to select it. Once you've done this, the mod list next to it will update to display all the mods available from this branch/tag. Select one of those, next, then press 'Install' to install the mod (or update it to the selected version, if it's already installed).

If you've installed the Launchpad correctly, and the mod was downloaded without any error messages, the only thing left to do is run the game.

## Advanced Techniques

### Uninstalling Mods

Once you have selected a mod, you can use "Edit -> Uninstall Mod" to remove that mod from your mod folder. This will prevent SR2 from detecting the mod, but will not delete the associated repository from your computer.

### Deleting Mod Repositories

You can also delete the currently opened repository via "Edit -> Delete Mod Repository". This will remove the repository from your computer (but will not uninstall the mod), and you will have to redownload it later before you can update/install mods from that repository again.

**WARNING:** The Launchpad does not make any attempt at determining whether you can redownload the repository again! Be careful before deleting local repos that haven't yet been pushed to a Git hosting service such as GitHub, GitLab, or Bitbucket, because you may not be able to recover the deleted data at all!

## Modder's Guide

Obviously, the Launchpad only supports Git repositories right now, and you'll need to know how to upload those to a hosting service such as GitHub, or host it on a server of your own. However, there's one more thing you should pay attention to: Your mod's file structure.

The Launchpad expects the mod's files to be contained inside a folder at the root of the repository, such as the `OpenSR` folder at the root of the OpenSR Modpack repository. It will search for the mod's `modinfo.txt` file, and only copy the folder containing that file.

If `modinfo.txt` is located in the repository's root directory, then the repository itself - and any other unnecessary files, such as `.gitignore` - will not be ignored, slowing the game down as it checks a bunch of useless files for meaningful data. This will also be problematic when trying to open the mod in the mod editor. Although this will no longer have much impact on your users, as the Launchpad now knows how to discard most repository metadata, it is still a considerable problem for the modder. Future releases of OpenSR may be able to offer additional mitigation, but it is better to address the problem at its source.

To avoid this issue, it is recommended that you follow the following instructions to create a symbolic link, storing the actual repository outside the game's mod folder without any negative effects: (Credit goes to Darloth for the original idea at https://github.com/DaloLorn/Rising-Stars/issues/20, and Skeletonxf for the Linux version)

### Windows Symlinking

1. Create a folder to keep the repository in. Example: `C:\Projects\SourceControl\SR2-Rising Stars`
2. Move your mod folder into the folder from step 1. Example: Move `C:\Program Files (x86)\Steam\SteamApps\common\Star Ruler 2\mods\Rising Stars` to `C:\Projects\SourceControl\SR2-Rising Stars\Rising Stars`
3. Using your preferred Git client or the Git command line, create a Git repository in the folder from step 1 and add the contents of your mod folder to the repository. Commit the added files. Example: 
    ```
        cd "C:\Projects\SourceControl\SR2-Rising-Stars"
        git init
        git commit -a -m "Initial commit"
    ```

    This should have added *everything* in the `SR2-Rising-Stars` folder to the repository - most importantly, it should have added the entire `Rising Stars` folder.

4. Close your Git client/command line, then open a command prompt, preferably an admin command prompt (just in case either the source or destination folder are in Program Files). Navigate to your Star Ruler 2 mods directory. Example:
    ```
        cd "C:\Program Files (x86)\Steam\SteamApps\common\Star Ruler 2\mods"
    ```
5. Create a symbolic link (specifically, a directory junction) from the mod folder in your Git repository to your SR2 mods directory. Example:
    ```
        mklink /J "Rising Stars" "C:\Projects\SourceControl\SR2-Rising Stars\Rising Stars"
    ```
   
### Linux Symlinking

1. Create a folder to keep the repository in. Example: `mkdir /opt/Projects/SourceControl/SR2-Rising-Stars`

2. Move your mod folder into the folder from step 1. Example: `mv "/opt/Star Ruler 2/mods/Rising Stars" "/opt/Projects/SourceControl/SR2-Rising-Stars/Rising Stars"`

3. Using your preferred Git client or the Git command line, create a Git repository in the folder from step 1 and add the contents of your mod folder to the repository. Commit the added files. Example:
    ```
        cd /opt/Projects/SourceControl/SR2-Rising-Stars
        git init
        git commit -a -m "Initial commit"
    ```
   
   This should have added *everything* in the `SR2-Rising-Stars` folder to the repository - most importantly, it should have added the entire `Rising Stars` folder.
   
4. Create a bind mount from the mod folder in your Git repository to your SR2 mods directory. Example:
    ```
        sudo mount --bind "/opt/Projects/SourceControl/SR2-Rising-Stars/Rising Stars" "/opt/Star Ruler 2/mods/Rising Stars" 
    ```
   
5. Once you have verified that the mounting was successful, open `/etc/fstab` (some distros may use a different file; consult your manual or other OS documentation) using a superuser text editor. Append a line with the syntax `SOURCE DESTINATION none bind` to the file. Save and exit; your OS should now automatically remount the folder on reboot. (Note that paths containing spaces must not be quoted, but must instead replace all spaces with the text `\040`!) Example:
    ```
        /opt/Projects/SourceControl/SR2-Rising-Stars/Rising\040Stars /opt/Star\040Ruler\0402/mods/Rising\040Stars none bind   
    ```
    
    
After all of these steps have been performed, both SR2 and your Git client should see the mod *exactly* the way they're meant to see it, and the Launchpad will be capable of cleanly downloading it from your preferred Git hosting service.

### Repository & Branch Descriptions

The OpenSR Launchpad pulls repository descriptions from a plaintext (or Markdown, though Markdown files will also be rendered as plaintext) readme file at the root of the repository, expected to be `README.md`. If it fails to find this, it will try the filenames `README.MD`, `Readme.md`, `readme.md`, `Readme.MD`, `readme.MD`, `readme.txt`, `Readme.txt`, `README.txt`, `README.TXT`, `Readme.TXT`, and `readme.TXT`, in the order listed. (JGit's path filtering does not allow for case insensitivity.) If none of these are present, your repository will not have a description in the repository info panel.

~~Aside from the repository description, the root directory of each branch or tag can have a `branch-description.txt` file (alternatives are `BRANCH-DESCRIPTION` or `Branch-Description`, extension is either fully lowercase or fully uppercase) which will describe that particular version of the mod. If none exists, the Launchpad will try to get the target version's readme file; if this is also missing from a given version of the repository, that version will not have a description in the branch info panel.~~ (Although this functionality is still fully supported, the below method may be easier to use with more complex repositories.)

Aside from the repository description, the root directory of each branch or tag can have a `branch-descriptions.json` JSON file (alternatives are `BRANCH-DESCRIPTIONS` or `Branch-Descriptions`, extension is either fully lowercase or fully uppercase) which will describe all versions of the mod. (Note that while I say "all versions", only the active branch's description will be pulled from the JSON.) If none exists, the Launchpad will try to get the target version's readme file; if this is also missing from a given version of the repository, that version will not have a description in the branch info panel.

An example JSON containing descriptions for the `master` and `WorkshopBuild` branches:

```json
{
    "master": "This is the main development branch for the OpenSR Modpack. If you're reading this description, and you're not specifically planning on testing the new MP version, you probably want the \"WorkshopBuild\" branch.",
    "WorkshopBuild": "This is the Workshop-tracking branch of the OpenSR Modpack. If it's on the Workshop, it's here - and vice versa."
}
```

### Repository Metadata

The Launchpad also supports other forms of repository metadata, via a `metadata.json` file (alternative names follow the same pattern as `branch-descriptions.json`, see above) in the repository root. This metadata has a multitude of functions, each of which will be described in more detail below.

#### Multimod Repositories

The Launchpad is capable of handling complex repositories containing multiple modinfo files (and, therefore, multiple mods). At the time of writing, this has not been an accepted standard in the OpenSR community, and only one such repository is known to exist. However, it is nonetheless fully supported by the Launchpad, so you are free to create such repositories as you see fit.

In order to properly create a multimod repository, you must register all of its constituent mods via the metadata `mods` field. If you do not do so, the Launchpad will default to detecting the first modinfo it can find in the repository, and ignore all other modinfos. The format is as follows:

```json
{
    "mods": {
        "Mod display name": {
            "rootFolder": "Path to the mod's modinfo.txt file.",
            "dependencies": ["Optional array of dependency entries - see Dependency Management below."]
        }
    }
}
```

For example, the OpenSR Modpack's metadata file:

```json
{
    "mods": {
        "OpenSR Modpack": {
            "rootFolder": "OpenSR"
        }
    }
}
```

#### Dependency Management

The Launchpad can be instructed to install dependencies for a mod, via a `dependencies` array. The syntax for a dependency is:

```json
{
    "name": "Display name of the dependency, for the Launchpad UI.",
    "repository": "Launchpad-readable Git URL for the dependency's repository.",
    "branch": "The branch/tag to use, as shown by the Launchpad's branch/tag list.",
    "modName": "Which mod to install from a multimod repository. If present, it must reference a mod entry from the target repository's metadata file."
}
```

And a minimalistic metadata file containing a dependency on the default mod of the OpenSR Modpack's `WorkshopBuild` branch looks like this:

```json
{
    "dependencies": [
        {
            "name": "OpenSR Modpack",
            "repository": "github.com/OpenSRProject-Modpack",
            "branch": "WorkshopBuild"
        }
    ]
}
```

Combining this concept with the `mods` field described in the Multimod Repositories section above, we can create a fully-formed metadata file for a multimod repository, such as this example file, created for a subset of the mods in the `DaloLorn/SR2Mods` repository:

```json
{
	"mods": {
		"[OBSOLETE] ABEM Modding Toolkit": {
			"rootFolder": "ABEMTool"
		},
		"Jraenar Imperium": {
			"rootFolder": "Jraenar"
		},
		"Koprulu Emergency Coalition": {
			"rootFolder": "KECSet",
			"dependencies": [
				{
					"name": "Trader Emergency Coalition",
					"repository": "github.com/DaloLorn/SR2Mods",
					"branch": "TECSet"
				},
				{
					"name": "Koprulu Navy",
					"repository": "github.com/DaloLorn/SR2Mods",
					"branch": "TerranSet"
				}
			]
		},
		"Multiple Theme Enabler": {
			"rootFolder": "MultiTheme"
		},
		"Khalai Unity": {
			"rootFolder": "ProtossAdventSet",
			"dependencies": [
				{
					"name": "Unity of the Advent",
					"repository": "github.com/DaloLorn/SR2Mods",
					"branch": "All-Mods",
					"modName": "Unity of the Advent"
				},
				{
					"name": "The Firstborn",
					"repository": "github.com/DaloLorn/SR2Mods",
					"branch": "All-Mods",
					"modName": "The Firstborn"
				}
			]
		}
	}
}
```

## Troubleshooting FAQ

Q: What's the difference between the standalone and standard versions of the OpenSR Launchpad?

>A: The standard version is smaller and (theoretically) more secure, because it relies on your computer having Java 11+ installed. (The added security stems from the fact that, in theory, you will periodically be updating Java. In theory.)
> 
> However, if for whatever reason you are unable or unwilling to install Java 11+, the standalone version includes a stripped-down JRE sufficient to run the Launchpad. None of the other differences have any practical meaning to you as a user, since they don't change how you run the application.  

Q: My computer gives me one of the following error messages:
```
ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the location of your Java installation.
```
```
ERROR: JAVA_HOME is set to an invalid directory: [VALUE OF JAVA_HOME]

Please set the JAVA_HOME variable in your environment to match the location of your Java installation.
```

>A: You are using the standard version of the Launchpad, and either you do not have Java installed, or it is not correctly installed. You will need to download whichever version of the Java Runtime Environment is appropriate for your operating system from [AdoptOpenJDK](https://adoptopenjdk.net) and try again. 
> 
> Take care to install Java 11 or newer, or you will still not be able to run the Launchpad. Another option would be to download the standalone version of the Launchpad, instead. 

Q: Java gives me an error message when I try to run this!

>A: Make sure your computer is running Java 11 or newer, or download the standalone version of the Launchpad instead of the standard version. If the error persists, contact the OpenSR team with the exact text of the error.

Q: When I try to use scripts from SR2MM 0.1.0, nothing happens!

>A: At this time, the OpenSR Launchpad no longer supports running in console mode. As a result, download scripts from SR2MM 0.1.0 are no longer usable.

Q: The program displayed an error message when I tried to do X!

>A: Many of the error/warning messages since 1.0.0 come with a recommended solution or workaround. (The 'encountered an exception' message is a notable exception.) If this is not the case, or the solution didn't work, contact the OpenSR team. Some of the possible exceptions *might* be verbose enough that you can figure it out on your own, but we make no promises.

Q: Help! My question's not listed here!

>A: I can't write an FAQ for questions I don't expect. Contact the OpenSR team, explain what's wrong, and hopefully we'll be able to figure it out.

## Contact

The best ways to contact the OpenSR team for Launchpad-related issues are, in order of relevance:

- Go to the OpenSR Discord server, https://discord.gg/sUJKJDc
- Open an issue at https://github.com/OpenSRProject/OpenStarRuler-Launchpad/issues or https://github.com/DaloLorn/SR2ModManager/issues
- Make a comment or forum post on the OpenSR Modpack's Workshop page, 
https://steamcommunity.com/sharedfiles/filedetails/?id=2340626368
