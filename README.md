# SR2 Mod Manager v1.0.0 Beta 1

A small, cross-platform tool capable of installing Star Ruler 2 mods uploaded to online Git repositories, such as the ones hosted by GitHub or Bitbucket.

The current version is a JavaFX GUI application, much more deserving of the 'manager' moniker than its predecessor.

## Table of Contents

1) Installation
2) Basic Use
3) Advanced Techniques
4) Modder's Guide
5) Troubleshooting FAQ
6) Contact

## Installation

In order to run this application, you must first have a computer with all of the following:

- Java Runtime Environment 1.8 or newer (Also known as Java 8)
- An Internet connection

The current .zip download contains the following file structure:

- SR2ModManager
    - dependencies
	    - *A folder containing a variety of .jar files required by the mod manager*
	- modmanager.bat
	- modmanager.sh
		- *A batch file which will start the mod manager (in case your computer doesn't know how to do it without hand-holding), as well as its Linux equivalent*
	- README.md
		- *You are here*
	- SR2ModManager.jar
		- *The mod manager itself*

To install the mod manager, copy the "SR2ModManager" folder into your Star Ruler 2 root directory (where "Star Ruler 2.exe" is located - and more importantly, where the "mods" folder is located). This will allow it to download the mods directly into the game's mod folder, saving you the extra step of copying the files over yourself.

## Basic Use

On Windows, your best bet is probably to run modmanager.bat. It avoids having to worry about whether Windows knows how to automatically run JAR files or not. (This is probably not a common issue, though.)

On Linux, the equivalent action is to run modmanager.sh. (Whether this actually makes sense is anyone's guess.)

To connect to a repository, enter its URL as if you were trying to access it via a browser, then press 'Connect'. If the protocol is omitted, SR2MM will default to HTTPS - similarly, if the '.git' file extension is missing, SR2MM will automatically append it to the URL. (Consequently, a link such as "github.com/DaloLorn/Rising-Stars" would be a perfectly valid way of accessing the Rising Stars repository.)

Once a repository has been opened, the branch list in the upper left section of the window will fill up with branch and tag names. Click on one of these to select it, then press 'Install' to install the mod (or update it to the selected version, if it's already installed).  

If you've installed the manager correctly, and the mod was downloaded without any error messages, the only thing left to do is run the game.

## Advanced Techniques

For now, there's not a lot of these to go around. You can use File->Open to open an existing repository (regardless of how the repository was downloaded), and Edit->Delete can delete the repository outright, but this latter one is less "advanced technique" and more "user beware". 

## Modder's Guide

Obviously, the mod manager only supports Git repositories right now, and you'll need to know how to upload those to a hosting service such as GitHub, or host it on a server of your own. However, there's one more thing you should pay attention to: Your mod's file structure.

The mod manager expects the mod's files to be contained inside a folder at the root of the repository, such as the "Rising Stars" folder at the root of the Rising Stars repository. It will search for the mod's modinfo.txt file, and only copy the folder containing that file.

If modinfo.txt is located in the repository's root directory, then the repository itself - and any other unnecessary files, such as .gitignore - will not be ignored, slowing the game down as it checks a bunch of useless files for meaningful data. This will also be problematic when trying to open the mod in the mod editor.

To avoid this issue, it is recommended that you follow the following instructions to create a symbolic link, storing the actual repository outside the game's mod folder without any negative effects: (Credit goes to Darloth for the original idea at https://github.com/DaloLorn/issues/20)

1. Create a folder to keep the repository in. Example: "C:\Projects\SourceControl\SR2-Rising Stars"
2. Move your mod folder into the folder from step 1. Example: Move "C:\Program Files (x86)\Steam\SteamApps\common\Star Ruler 2\mods\Rising Stars" to "C:\Projects\SourceControl\SR2-Rising Stars\Rising Stars"
3. Using your preferred Git client or the Git command line, create a Git repository in the folder from step 1 and add the contents of your mod folder to the repository. Commit the added files. Example: 
    ```
	    cd "C:\Projects\SourceControl\SR2-Rising Stars"
	    git init
	    git commit -a -m "Initial commit"
    ```
    This should have added *everything* in the "SR2-Rising Stars" folder to the repository - most importantly, it should have added the entire "Rising Stars" folder.

4. Close your Git client/command line, then open a command prompt, preferably an admin command prompt (just in case either the source or destination folder are in Program Files). Navigate to your Star Ruler 2 mods directory. Example:
    ```
	    cd "C:\Program Files (x86)\Steam\SteamApps\common\Star Ruler 2\mods"
    ```
5. Create a symbolic link (specifically, a directory junction) from the mod folder in your Git repository to your SR2 mods directory. Example:
    ```
	    mklink /J "Rising Stars" "C:\Projects\SourceControl\SR2-Rising Stars\Rising Stars"
    ```
    
After all of these steps have been performed, both SR2 and your Git client should see the mod *exactly* the way they're meant to see it, and this mod manager will be capable of cleanly downloading it from your preferred Git hosting service.

### Descriptions

SR2MM pulls mod descriptions from a plaintext (or Markdown, though Markdown files will also be rendered as plaintext) readme file at the root of the repository, expected to be README.md. If it fails to find this, it will try the filenames README.MD, Readme.md, readme.md, Readme.MD, readme.MD, readme.txt, Readme.txt, README.txt, README.TXT, Readme.TXT, and readme.TXT, in the order listed. (JGit's path filtering does not allow for case insensitivity.) If none of these are present, your mod will not have a description in the mod info panel.

Aside from the mod description, the root directory of each branch or tag can have a branch-description.txt file (alternatives are BRANCH-DESCRIPTION or Branch-Description, extension is either fully lowercase or fully uppercase) which will describe that particular version of the mod. If none exists, SR2MM will try to get the target version's readme file; if this is also missing from a given version of the repository, that version will not have a description in the branch info panel.

## Troubleshooting FAQ

Q: My computer says `'java' is not recognized as an internal or external command, operable program or batch file.` or something like that!

>A: You do not have Java installed, or it is not correctly installed. You will need to download whichever version of the Java Runtime Environment is appropriate for your operating system from http://www.oracle.com/technetwork/java/javase/downloads/index.html and try again.

Q: Java gives me an error message when I try to run this!

>A: Make sure your computer is running Java 8 or newer. If the error persists, contact me with the exact text of the error.

Q: When I try to use scripts from SR2MM 0.1.0, nothing happens!

>A: At this time, SR2MM no longer supports running in console mode. As a result, download scripts from 0.1.0 are no longer usable.

Q: The program displayed an error message when I tried to do X!

>A: Many of the error/warning messages in 1.0.0 come with a recommended solution or workaround. (The 'encountered an exception' message is a notable exception.) If this is not the case, or the solution didn't work, contact me. Some of the possible exceptions *might* be verbose enough that you can figure it out on your own, but I make no promises.

Q: Uhh, this isn't the mod I wanted. What's going on?

>A: The mod manager currently expects there to be *only one* modinfo.txt file in the entire repository. If this is not the case, it may result in unpredictable behavior without causing any errors that the manager can detect.

Q: Help! My question's not listed here!

>A: I can't write an FAQ for questions I don't expect. Contact me, explain what's wrong, and hopefully we'll be able to figure it out.

## Contact

If you want a response (and you probably do, why else would you be trying to contact me?), then ModDB is a bad idea. You *can*, however, try one of these:

- Open an issue at https://github.com/DaloLorn/Rising-Stars/issues or https://github.com/DaloLorn/SR2ModManager/issues
- Make a comment or forum post on the Rising Stars Workshop page, 
http://steamcommunity.com/sharedfiles/filedetails/?id=812827373
- Go to the Rising Stars Discord server, https://discord.gg/sUJKJDc