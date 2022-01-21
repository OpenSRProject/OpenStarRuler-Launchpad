package io.github.openstarruler.launchpad.adapter

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RevisionSyntaxException
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.ObjectLoader
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter
import java.io.*
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.stream.Collectors
import kotlin.io.path.absolute

/** Utility class containing a number of helper functions.  */
object Utils {
    /** Iterates through a list of likely variations on the filename 'branch-description.txt', which is the expected branch description.
     *
     * If none exists, falls back to the readme file.  */
    @Throws(FileNotFoundException::class)
    fun generateBranchDescWalker(repo: Repository?, tree: ObjectId?): TreeWalk {
        var result: TreeWalk? = null
        try {
            result = TreeWalk.forPath(repo, "branch-descriptions.json", tree)
            if (result == null) result = TreeWalk.forPath(repo, "Branch-Descriptions.json", tree)
            if (result == null) result = TreeWalk.forPath(repo, "BRANCH-DESCRIPTIONS.json", tree)
            if (result == null) result = TreeWalk.forPath(repo, "branch-description.txt", tree)
            if (result == null) result = TreeWalk.forPath(repo, "Branch-Description.txt", tree)
            if (result == null) result = TreeWalk.forPath(repo, "BRANCH-DESCRIPTION.txt", tree)
            if (result == null) result = TreeWalk.forPath(repo, "BRANCH-DESCRIPTIONS.JSON", tree)
            if (result == null) result = TreeWalk.forPath(repo, "Branch-Descriptions.JSON", tree)
            if (result == null) result = TreeWalk.forPath(repo, "branch-descriptions.JSON", tree)
            if (result == null) result = TreeWalk.forPath(repo, "BRANCH-DESCRIPTION.TXT", tree)
            if (result == null) result = TreeWalk.forPath(repo, "Branch-Description.TXT", tree)
            if (result == null) result = TreeWalk.forPath(repo, "branch-description.TXT", tree)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (result == null) {
            System.err.println("Could not find branch-description.txt, attempting to pull README.md instead...")
            result = generateReadmeWalker(repo, tree)
        }
        return result
    }

    val IS_WINDOWS = System.getProperty("os.name").lowercase().trim { it <= ' ' }.startsWith("win")

    /** Iterates through a list of likely readme names. I'd use case-insensitive filtering, but I don't fancy figuring out how to write a case-insensitive version of PathFilter.  */
    @Throws(FileNotFoundException::class)
    fun generateReadmeWalker(repo: Repository?, tree: ObjectId?): TreeWalk {
        var result: TreeWalk? = null
        try {
            result = TreeWalk.forPath(repo, "README.md", tree)
            if (result == null) result = TreeWalk.forPath(repo, "README.MD", tree)
            if (result == null) result = TreeWalk.forPath(repo, "Readme.md", tree)
            if (result == null) result = TreeWalk.forPath(repo, "readme.md", tree)
            if (result == null) result = TreeWalk.forPath(repo, "Readme.MD", tree)
            if (result == null) result = TreeWalk.forPath(repo, "readme.MD", tree)
            if (result == null) result = TreeWalk.forPath(repo, "readme.txt", tree)
            if (result == null) result = TreeWalk.forPath(repo, "Readme.txt", tree)
            if (result == null) result = TreeWalk.forPath(repo, "README.txt", tree)
            if (result == null) result = TreeWalk.forPath(repo, "README.TXT", tree)
            if (result == null) result = TreeWalk.forPath(repo, "Readme.TXT", tree)
            if (result == null) result = TreeWalk.forPath(repo, "readme.TXT", tree)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (result == null) throw FileNotFoundException("Could not find README.md!")
        return result
    }

    fun countFiles(source: Path): Int {
        var count = 0
        Files.walkFileTree(source.absolute(), object: SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                count++
                return FileVisitResult.CONTINUE
            }
        })
        return count
    }

    @Throws(FileNotFoundException::class)
    fun generateModinfoWalker(repo: Repository?, tree: ObjectId?, root: String?): TreeWalk {
        var result: TreeWalk? = null
        try {
            if(root != null) {
                result = TreeWalk.forPath(repo, "$root/modinfo.txt", tree)
            }
            else {
                result = TreeWalk(repo).apply {
                    addTree(tree)
                    isRecursive = true
                    filter = PathSuffixFilter.create("modinfo.txt")
                }
                if(!result.next())
                    result = null
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if(result == null) throw FileNotFoundException("Could not find modinfo.txt at path '$root'!")
        return result
    }

    /** Deletes a folder and all of its contents from the hard drive.  */
    fun deleteFolder(folder: File) {
        if (!folder.exists()) return  // The folder's not null, but that doesn't mean it exists.
        val files = folder.listFiles()
        if (files != null) { //some JVMs return null for empty dirs
            for (f in files) {
                if (f.isDirectory) {
                    deleteFolder(f)
                } else {
                    f.delete()
                }
            }
        }
        folder.delete()
    }

    fun readGitFile(fileLoader: ObjectLoader): String? {
        try {
            fileLoader.openStream().use { fileStream ->
                BufferedReader(InputStreamReader(fileStream)).use { fileReader ->
                    return fileReader.lines().collect(Collectors.joining("\n"))
                }
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
            return null
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    /** Iterates through a list of likely metadata filenames. I'd use case-insensitive filtering, but I don't fancy figuring out how to write a case-insensitive version of PathFilter.  */
    @Throws(FileNotFoundException::class)
    fun generateMetadataWalker(repo: Repository?, tree: ObjectId?): TreeWalk {
        var result: TreeWalk? = null
        try {
            result = TreeWalk.forPath(repo, "metadata.json", tree)
            if (result == null) result = TreeWalk.forPath(repo, "METADATA.JSON", tree)
            if (result == null) result = TreeWalk.forPath(repo, "Metadata.json", tree)
            if (result == null) result = TreeWalk.forPath(repo, "METADATA.json", tree)
            if (result == null) result = TreeWalk.forPath(repo, "Metadata.JSON", tree)
            if (result == null) result = TreeWalk.forPath(repo, "metadata.JSON", tree)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (result == null) throw FileNotFoundException("Could not find metadata.json!")
        return result
    }

    @Throws(RevisionSyntaxException::class, IOException::class)
    fun getLoader(repo: Git, branch: String, walkerGenerator: (Repository, ObjectId) -> TreeWalk): ObjectLoader {
        val repository = repo.repository
        val treeId = repository.resolve("$branch^{tree}")
        val walker = walkerGenerator(repository, treeId)
        val descriptionId = walker.getObjectId(0)
        return repository.open(descriptionId)
    }
}
