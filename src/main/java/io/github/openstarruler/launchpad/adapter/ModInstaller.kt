package io.github.openstarruler.launchpad.adapter

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import io.github.openstarruler.launchpad.adapter.ModInstaller.TextHandler
import io.github.openstarruler.launchpad.model.Modinfo
import io.github.openstarruler.launchpad.model.RepoMetadata
import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.api.errors.RefNotAdvertisedException
import org.eclipse.jgit.errors.NotSupportedException
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.ObjectLoader
import org.eclipse.jgit.lib.Ref
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

object ModInstaller {
    private const val NO_BRANCH_DESC = "No description could be found for this branch."
    private const val ENCOUNTERED_AN_EXCEPTION = "Encountered an exception: "
    private const val PROTOCOL_GIT = "git://"
    private const val PROTOCOL_HTTPS = "https://"
    private const val NO_REPO_DESC = "No description could be found for this repository."

    private var repo: Git? = null
    private var currentBranch: Ref? = null
    private var currentMetadata: RepoMetadata? = null
        get() = if (currentBranch != null) field ?: RepoMetadata() else null
    private val branches: MutableMap<String, Ref> = mutableMapOf()

    fun hasRepo() = repo != null

    fun hasBranch() = currentBranch != null

    fun setActiveBranch(branchName: String?): String {
        currentMetadata = null

        currentBranch = branches[branchName]
        val descriptionLoader: ObjectLoader? = try {
            Utils.getLoader(repo!!, currentBranch!!.name, Utils::generateBranchDescWalker)
        } catch (e: Exception) {
            e.printStackTrace()
            return NO_BRANCH_DESC
        }

        try {
            val metaLoader = Utils.getLoader(repo!!, currentBranch!!.name, Utils::generateMetadataWalker)
            val json = Utils.readGitFile(metaLoader)
            if (json != null) {
                currentMetadata = Gson().fromJson(json, RepoMetadata::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val json = descriptionLoader?.let { Utils.readGitFile(it) }
        return if (json != null) {
            try {
                val descriptions =
                    Gson().fromJson<Map<String, String>>(json, object : TypeToken<HashMap<String?, String?>?>() {}.type)
                if (descriptions != null && descriptions.isNotEmpty()) {
                    descriptions[branchName] ?: NO_BRANCH_DESC
                } else {
                    NO_BRANCH_DESC
                }
            } catch (e: JsonSyntaxException) {
                json
            }
        } else NO_BRANCH_DESC
    }

    fun connectToRepository(
        url: String,
        progressHandler: TextHandler?,
        errorHandler: TextHandler?
    ): String? {
        var url = url
        return try {
            progressHandler?.handle("Parsing URL...")
            if (url == "") {
                errorHandler?.handle("URL field is empty! Cannot connect to the cold void of space.\n\nPlease enter the URL of the Git repository you want to connect to.")
                return null
            }
            url = parseRepoURL(url)
            val localRoot = getLocalRoot(url)
            progressHandler?.handle("Loading repository...")
            val tmp = cloneRepository(localRoot, url)
            if (repo != null)
                repo!!.close()
            currentBranch = null
            currentMetadata = null
            repo = tmp
            url
        } catch (e: Exception) {
            if (errorHandler != null) {
                if (e.cause is NotSupportedException && (e.cause as NotSupportedException).message!!.startsWith("URI not supported: "))
                    errorHandler.handle("Not a valid Git URL!\n\nThe URL provided isn't a valid Git URL.\n\nPlease make sure you entered the correct URL, and contact the SR2MM developers if the problem persists.")
                else
                    errorHandler.handle(ENCOUNTERED_AN_EXCEPTION + e)
            }
            e.printStackTrace()
            null
        }
    }

    @Throws(GitAPIException::class)
    private fun cloneRepository(localRoot: Path, url: String): Git {
        return try {
            Git.open(localRoot.toFile())
        } catch (e: IOException) {
            val cmd = Git.cloneRepository().setDirectory(localRoot.toFile())
            try {
                cmd.setURI(url).call()
            } catch (e2: Exception) {
                // Apparently the Git protocol isn't yet supported by all Git servers.
                // So if the clone failed, it might not mean an HTTPS clone would do the same.
                if (url.startsWith(PROTOCOL_GIT)) {
                    cmd.setURI(url.replace(PROTOCOL_GIT, PROTOCOL_HTTPS)).call()
                } else throw e2
            }
        }
    }

    fun openRepository(dir: File, errorHandler: TextHandler): String? {
        return try {
            val tmp = Git.open(dir)
            if (repo != null) repo!!.close()
            repo = tmp
            var url = repo!!.repository.config.getString("remote", "origin", "url")
            if (url == null) url = "a local repository at " + dir.absolutePath
            url
        } catch (e: RepositoryNotFoundException) {
            e.printStackTrace()
            errorHandler.handle("Not a valid repository!")
            null
        } catch (e: Exception) {
            e.printStackTrace()
            errorHandler.handle(ENCOUNTERED_AN_EXCEPTION + e)
            null
        }
    }

    fun getBranches(errorHandler: TextHandler?): List<String>? {
        return try {
            repo!!.fetch().call()
            val tags = repo!!.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call()
            tags.addAll(repo!!.tagList().call())
            val tagNames: MutableList<String> = mutableListOf()
            branches.clear()
            for (tag in tags) {
                val tagName = tag.name.replaceFirst("refs/remotes/origin/|refs/tags/".toRegex(), "")
                tagNames.add(tagName)
                branches[tagName] = tag
            }
            tagNames
        } catch (e: Exception) {
            errorHandler?.handle(ENCOUNTERED_AN_EXCEPTION + e)
            e.printStackTrace()
            null
        }
    }

    fun getDescription(errorHandler: TextHandler?): String? {
        return try {
            val description = Utils.readGitFile(Utils.getLoader(repo!!, "master", Utils::generateReadmeWalker))
            description ?: NO_REPO_DESC
        } catch (e: Exception) {
            errorHandler?.handle(ENCOUNTERED_AN_EXCEPTION + e)
            e.printStackTrace()
            null
        }
    }

    private fun parseRepoURL(url: String): String {
        var url = url
        if (!url.startsWith("http", true) && !url.startsWith(PROTOCOL_GIT, true))
            url = PROTOCOL_HTTPS + url
        if (!url.endsWith(".git", true))
            url += ".git"
        return url
    }

    @Throws(IOException::class)
    private fun getLocalRoot(url: String): Path {
        val path = url.split("/").toTypedArray()
        var repoName = path[path.size - 2] + "_" + path[path.size - 1]
        repoName = repoName.substring(0, repoName.length - 4)
        var localRoot = Path("repositories", repoName)
        if (!localRoot.exists() || !localRoot.isDirectory()) localRoot =
            Files.createDirectories(localRoot)
        return localRoot
    }

    @Throws(IOException::class)
    private fun installModImpl(
        repo: Git,
        warningHandler: TextHandler,
        progressHandler: TextHandler,
        infoHandler: TextHandler?,
        errorHandler: TextHandler,
        modName: String?
    ) {
        val root: Path = repo.repository.workTree.toPath()
        progressHandler.handle("Parsing installation instructions...")
        val metadata = root.absolute() / "metadata.json"
        var meta: RepoMetadata? = null
        if (metadata.exists()) {
            try {
                Files.newBufferedReader(metadata).use { reader ->
                    meta = Gson().fromJson(reader, RepoMetadata::class.java)
                    meta?.dependencies?.forEach { dependency ->
                        if (!installDependency(dependency, progressHandler, warningHandler))
                            warningHandler.handle("WARNING: Failed to install dependency ${dependency.name}!\n\nThe mod may behave erratically or fail to start. Please try to install the dependency manually, or contact the mod (and/or dependency) developers.")
                    }
                }
            } catch (e: Exception) {
                warningHandler.handle("WARNING: Unable to read installation instructions!\n\nThis mod's repository contains an additional metadata file (metadata.json) with additional information required for a successful installation, such as the locations of any prerequisite mods.\nPlease review the metadata file and take the necessary actions, or contact the mod developer.")
                e.printStackTrace()
                Desktop.getDesktop().open(metadata.toFile())
            }
        }

        progressHandler.handle("Finding modinfo...")
        val modinfo = findModinfo(root, warningHandler, meta, modName)
        if (modinfo == null) {
            reportMissingModinfo(errorHandler, modName)
            return
        }

        progressHandler.handle("Found modinfo, preparing installation directory...")
        val source =
            if (modinfo.inRoot) root
            else root / modinfo.folderName
        val destination = findModInstallDir(modinfo)
        // Wipe out any previous installation, just to be sure.
        Utils.deleteFolder(destination.toFile())
        if (destination.exists()) { // We failed to delete the previous installation.
            errorHandler.handle("Could not delete previous mod installation!\n\nA possible workaround might be to delete the mod folder yourself, then try again.")
            return
        }

        progressHandler.handle("Copying mod files from repository...")
        destination.createDirectories()
        Files.walkFileTree(source.absolute(), CopyFileVisitor(destination.toAbsolutePath()))
        infoHandler?.handle("Mod successfully installed!")
    }

    private fun reportMissingModinfo(
        errorHandler: TextHandler,
        modName: String?
    ) {
        if (modName != null) {
            errorHandler.handle(
                String.format(
                    "Cannot find modinfo.txt!%n%nThis repository does not appear to have registered a valid Star Ruler 2 mod under the name \"%s\".%nPlease make sure that you have connected to the right repository, and contact the mod developer if the issue persists.",
                    modName
                )
            )
        } else {
            errorHandler.handle("Cannot find modinfo.txt!\n\nThis repository does not appear to contain a valid Star Ruler 2 mod.\nPlease make sure that you have connected to the right repository, and contact the mod developer if the issue persists.")
        }
    }

    @Throws(IOException::class)
    private fun findModinfo(
        root: Path,
        warningHandler: TextHandler?,
        meta: RepoMetadata?,
        modName: String?
    ): Modinfo? {
        var inRoot = false
        val folderName: String
        val finder = SingleFinder("modinfo.txt")
        var origin = root
        if (meta != null && modName != null) {
            val mod = meta.mods?.get(modName)
            if (mod?.rootFolder != null && mod.rootFolder != "") origin /= mod.rootFolder!!
        }
        Files.walkFileTree(origin.absolute(), finder)
        return if (finder.result != null) {
            folderName = finder.result!!.parent.fileName.toString()
            if (Files.isSameFile(finder.result!!.parent, root)) {
                inRoot = true // The modinfo is in the repository root, so we can't discard metadata.
                warningHandler?.handle("WARNING: Unable to discard repository metadata!\n\nTo improve loading times, it is recommended that you delete the installed mod's .git folder once installation is completed.")
            }
            Modinfo(inRoot, folderName, finder.result!!.toFile())
        } else {
            null
        }
    }

    @Throws(IOException::class)
    private fun findGitModinfo(
        root: Path?,
        warningHandler: TextHandler?
    ): Modinfo? {
        val repository = repo!!.repository
        val treeId = repository.resolve("${currentBranch!!.name}^{tree}")
        val walker = Utils.generateModinfoWalker(repository, treeId, root)
        val inRoot = walker.pathString == "modinfo.txt"
        val modinfoId = walker.getObjectId(0)
        val loader = repository.open(modinfoId)
        return Modinfo(inRoot, walker.pathString.removeSuffix("modinfo.txt"), loader)
    }

    private fun findModInstallDir(
        modinfo: Modinfo
    ): Path {
        return Settings.instance.modsFolder / modinfo.folderName
    }

    private fun installDependency(
        dependency: RepoMetadata.Dependency,
        progressHandler: TextHandler,
        warningHandler: TextHandler
    ): Boolean {
        return try {
            progressHandler.handle("Parsing URL for dependency \"" + dependency.name + "\"...")
            if (dependency.repository == null || dependency.repository == "") {
                warningHandler.handle(
                    String.format(
                        "Failed to install dependency \"%s\": Dependency metadata doesn't specify a URL. The author may be playing a prank on his users.",
                        dependency.name
                    )
                )
                return false
            }
            val url = parseRepoURL(dependency.repository!!)
            val localRoot = getLocalRoot(url)
            progressHandler.handle("Loading dependency \"" + dependency.name + "\"...")
            val depRepo = cloneRepository(localRoot, url)
            depRepo.checkout()
                .setName(dependency.branch)
                .setCreateBranch(depRepo.repository.resolve("refs/heads/" + dependency.branch) == null)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                .setStartPoint("refs/remotes/origin/" + dependency.branch)
                .call()
            depRepo.pull().call()
            val internalWarningHandler = TextHandler { text ->
                warningHandler.handle(
                    String.format(
                        "Encountered warning while installing dependency \"%s\": %s",
                        dependency.name,
                        text
                    )
                )
            }
            installModImpl(
                depRepo,
                internalWarningHandler,
                { text ->
                    var progress = text
                    if (!progress.startsWith("Installing dependency")) progress =
                        String.format("Installing dependency \"%s\": %s", dependency.name, progress)
                    progressHandler.handle(progress)
                },
                null,
                { text ->
                    warningHandler.handle(
                        String.format(
                            "Failed to install dependency \"%s\": %s",
                            dependency.name,
                            text
                        )
                    )
                },
                dependency.modName
            )
            true
        } catch (e: Exception) {
            if (e.cause is NotSupportedException && (e.cause as NotSupportedException).message!!.startsWith("URI not supported: ")) warningHandler.handle(
                String.format(
                    "Failed to install dependency \"%s\": Not a valid Git URL!%n%nThe URL in the dependency metadata is not a valid Git URL.%n%nPlease contact the mod author and/or SR2MM developers.",
                    dependency.name
                )
            ) else warningHandler.handle(
                String.format("Failed to install dependency \"%s\": %s", dependency.name, e)
            )
            e.printStackTrace()
            false
        }
    }

    fun installMod(
        warningHandler: TextHandler,
        progressHandler: TextHandler,
        infoHandler: TextHandler?,
        errorHandler: TextHandler?
    ) {
        try {
            progressHandler.handle("Checking out target branch or tag...")
            val isTag = currentBranch!!.name.startsWith("refs/tags/")
            val createBranch = !isTag && repo!!.repository.resolve(
                currentBranch!!.name.replaceFirst(
                    "refs/remotes/origin".toRegex(),
                    "refs/heads"
                )
            ) == null
            repo!!.checkout()
                .setName(currentBranch!!.name.replaceFirst("refs/remotes/origin/".toRegex(), ""))
                .setCreateBranch(createBranch)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                .setStartPoint(currentBranch!!.name)
                .call()
            if (!isTag) {
                progressHandler.handle("Pulling upstream changes...")
                repo!!.pull().call()
            }
            installModImpl(repo!!, warningHandler, progressHandler, infoHandler, errorHandler!!, null)
        } catch (e: RefNotAdvertisedException) {
            errorHandler!!.handle("This branch or tag is no longer visible. It may have been deleted from the origin repository, or it may have been hidden somehow.\n\nThis may often be the case for branches used in beta testing or miscellaneous development; try another one, or contact the mod developers.")
            e.printStackTrace()
        } catch (e: Exception) {
            errorHandler!!.handle(ENCOUNTERED_AN_EXCEPTION + e)
            e.printStackTrace()
        }
    }

    fun deleteRepository(errorHandler: TextHandler): Boolean {
        val root = repo!!.repository.workTree
        repo!!.close()
        repo = null
        Utils.deleteFolder(root)
        return if (root.exists()) {
            errorHandler.handle("Could not delete repository!\n\nFor some reason, the repository was not deleted. You may have to delete it yourself.")
            false
        } else {
            branches.clear()
            true
        }
    }

    fun uninstallMod(errorHandler: TextHandler, modName: String?): Boolean {
        val root = repo!!.repository.workTree.toPath()
        var modinfo: Modinfo? = null
        try {
            modinfo = findModinfo(root, null, currentMetadata, modName)
        } catch (e: Exception) {
            errorHandler.handle("Encountered an exception trying to find the modinfo file!\n\nFor some reason, the mod folder couldn't be detected. You may have to delete it yourself.")
        }
        if (modinfo == null) return false
        val installDir = findModInstallDir(modinfo)
        Utils.deleteFolder(installDir.toFile())
        return if (installDir.exists()) {
            errorHandler.handle("Could not delete mod folder!\n\nFor some reason, the mod folder couldn't be deleted. You may have to delete it yourself.")
            false
        } else {
            true
        }
    }

    fun getModDescription(modName: String?): String {
        val modinfo = findGitModinfo(currentMetadata?.mods?.get(modName)?.rootFolder?.let { Path(it) / "" } , null)
        return if (modinfo != null) {
            modinfo.description ?: "That's weird. This mod has no description.\n\nWell, it's a Star Ruler 2 mod, at any rate!"
        } else {
            "No modinfo could be found for this mod! This is not a valid Star Ruler 2 mod!"
        }
    }

    fun listMods(): Map<String, RepoMetadata.Mod> {
        return currentMetadata?.mods!!
    }

    fun interface TextHandler {
        fun handle(text: String)
    }
}
