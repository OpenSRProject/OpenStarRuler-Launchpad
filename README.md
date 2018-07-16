# SR2 Mod Manager v0.2.0

A small, cross-platform tool capable of installing Star Ruler 2 mods uploaded to online Git repositories, such as the ones hosted by GitHub or Bitbucket.

The current version can be run from the command line or a basic Swing GUI. 

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
	- Download Rising Stars.bat
	- Download Rising Stars.sh
		- *A batch file preconfigured to download the current Workshop version of Rising Stars from GitHub, as well as its Linux equivalent*
	- modmanager.bat
	- modmanager.sh
		- *A batch file which will start the mod manager (for those who don't want to start it the 'hard' way, like me), as well as its Linux equivalent*
	- Readme.txt
		- *You are here*
	- SR2ModManager.jar
		- *The mod manager itself. Unfortunately, this will not run correctly unless started in a command prompt (or PowerShell, or a Linux equivalent), which is where the abovementioned batch files and shell scripts come into play.*

To install the mod manager, copy the "SR2ModManager" folder into your Star Ruler 2 root directory (where "Star Ruler 2.exe" is located - and more importantly, where the "mods" folder is located). This will allow it to download the mods directly into the game's mod folder, saving you the extra step of copying the files over yourself.

## Basic Use

On Windows, your best bet is probably to run modmanager.bat. It will take care of the more annoying aspects of starting the mod manager through the command line, and simply ask you to provide the repository URL and the name of the branch you want to download from.

On Linux, the equivalent action is to run modmanager.sh. It, too, should just ask for a repository URL and branch name before jumping straight to work.

The default parameters (also used explicitly in the "Download Rising Stars" script) are as follows:

Repository URL: https://github.com/DaloLorn/Rising-Stars.git
Branch name: WorkshopBuild

When downloading from GitHub, the repository URL can be found by clicking on the "Clone or download" button and copying the link that pops up. The branch name, however, depends exclusively on how the repository is structured and which version of the mod you want to use.

Once you've input all the necessary parameters, the mod manager will start downloading the repository. This may take a few minutes, depending on the size of the repository and the speed of your connection, but once it reports "Retrieved repository for mod: So-and-so", your mod will be in the "mods" folder just outside the folder the manager is located in.

If you've installed the manager correctly, and the mod was downloaded without any error messages, the only thing left to do is run the game.

## Advanced Techniques

For now, there's not a lot of these to go around. However, the "Download Rising Stars" files are a decent example of how to write a script that will automatically download from a specific repository and branch, which can be useful if you expect to update the mod on a regular basis.

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

## Troubleshooting FAQ

Q: My computer says `'java' is not recognized as an internal or external command, operable program or batch file.` or something like that!

>A: You do not have Java installed, or it is not correctly installed. You will need to download whichever version of the Java Runtime Environment is appropriate for your operating system from http://www.oracle.com/technetwork/java/javase/downloads/index.html and try again.

Q: Java gives me an error message when I try to run this!

>A: Make sure your computer is running Java 8 or newer. If the error persists, contact me with the exact text of the error.

Q: When I try to run the program, nothing happens!

>A: You're probably trying to run it by right-clicking SR2ModManager.jar. This doesn't currently work - either use the command line or right-click modmanager.bat (or modmanager.sh on Linux).

Q: The program throws an exception saying "Exception in thread "main" java.io.IOException: Could not delete temporary file %FileName", what do I do?

>A: I'm not exactly sure. This means that it tried to create and clear up a temporary folder to download the Git repository, but didn't have enough privileges to delete the folder.
>
>Running as administrator *might* help, but I'm hoping this will never actually happen.

Q: The program says "ERROR: Could not find modinfo.txt!", what does that mean?

>A: Simply put, you don't seem to be downloading a valid SR2 mod. Try a different repository, or inform the author of the mod that he's *somehow* missing the most important file.

Q: The program said "ERROR: Could not delete previous mod installation!", now what?

>A: In order to avoid bugs, the mod manager has to completely delete the contents of whatever folder it's copying the mod into - including copies it had downloaded itself. For some reason, it was unable to do so.
>
>If this occurs, you have two options: Either make sure there's no reason the folder would be inaccessible (run as administrator, close Star Ruler 2, close anything else accessing the mod's files), or delete the folder yourself.
>
>If the problem persists, contact me and we can try to figure out what's blocking it.

Q: The program said "WARNING: Could not delete temporary folders!". Is this a problem? Should I do anything?

>A: Strictly speaking, yes, this is a problem. It means the mod manager failed to clean up after itself, so you now have an unused Git repository taking up a tiny chunk of your disk space. However, if you have enough disk space to spare, then you don't *have* to do anything right away.
>
>If, on the other hand, you're feeling like cleaning it up, Disk Cleanup should catch those files (and possibly many others) just fine. If it doesn't, the manager *does* output the location of the temporary folder, so you should be able to track it down and delete it manually. Nothing to worry about.

Q: The program said "WARNING: Unable to discard repository metadata!". What does this mean?

>A: The Git repository you're downloading from isn't structured in a way that the mod manager supports. Because of this, unnecessary data such as the ".git" folder will be copied into your mod folder along with the mod itself, resulting in slower loading times.
>
>If you know how to recognize it, you should probably delete the extra files. If not... your best bet is probably to tell the mod author to read the instructions in this readme.

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