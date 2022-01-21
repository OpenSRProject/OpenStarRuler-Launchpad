package io.github.openstarruler.launchpad.adapter

import io.github.openstarruler.launchpad.model.Release
import okhttp3.OkHttpClient
import okhttp3.Request
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand.ResetType
import org.eclipse.jgit.transport.URIish
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import java.util.zip.ZipFile
import kotlin.concurrent.withLock

object OpenSRManager {
    val openSRVersions: List<Release> = GitHub.getReleases(repo = "OpenStarRuler")

    fun installOpenSR(
        version: Release,
        warningHandler: TextHandler?,
        errorHandler: TextHandler?,
        progressHandler: TextHandler?
    ) {
        progressHandler?.handle("Detecting OS version...")
        val type = if (Utils.IS_WINDOWS) "windows" else "linux"
        progressHandler?.handle("Searching for binaries...")
        val zipAsset = version.assets.find { it.name.contains(type) }
        if(zipAsset == null) {
            errorHandler?.handle("Failed to find platform-appropriate binary package for selected version!\n\nPlease try installing a different version of OpenSR, and report this issue to the OpenSR team.")
            return
        }

        progressHandler?.handle("Downloading binary package...")
        val okHttpClient = OkHttpClient.Builder()
            .build()
        val request = Request.Builder()
            .url(zipAsset.downloadUrl)
            .build()
        okHttpClient.newCall(request).execute().body?.byteStream().use { zipBuffer ->
            if(zipBuffer == null) {
                errorHandler?.handle("Failed to download platform-appropriate binary package for selected version!\n\nPlease try installing a different version of OpenSR, and report this issue to the OpenSR team.")
                return
            }
            progressHandler?.handle("Saving binary package...")
            FileOutputStream("opensr-binaries.zip", false).use { zipBuffer.copyTo(it) }
        }

        progressHandler?.handle("Connecting to OpenSR data repository...")
        val dataRepo = try {
            Git.open(File(Settings.instance.gamePath))
        } catch (e: IOException) {
            Git.init().setDirectory(File(Settings.instance.gamePath)).call()
        }
        dataRepo.use {
            val remotes = dataRepo.remoteList().call()
            val origin = remotes.find { it.name == "origin" }
            if (origin == null)
                dataRepo.remoteAdd()
                    .setName("origin")
                    .setUri(URIish("https://github.com/OpenSRProject/OpenStarRuler-Data.git"))
                    .call()
            else if (origin.urIs.find { it.toString() == "https://github.com/OpenSRProject/OpenStarRuler-Data.git" } == null)
                dataRepo.remoteSetUrl()
                    .setRemoteName("origin")
                    .setRemoteUri(URIish("https://github.com/OpenSRProject/OpenStarRuler-Data.git"))
                    .call()

            if (version.tagName == "nightly") {
                dataRepo.fetch()
                    .setProgressMonitor(GitProgressHandler("Downloading latest data...", progressHandler))
                    .call()

                dataRepo.reset()
                    .setProgressMonitor(GitProgressHandler("Installing latest data...", progressHandler))
                    .setMode(ResetType.HARD)
                    .setRef("refs/remotes/origin/master")
                    .call()
            } else try {
                dataRepo.fetch()
                    .setProgressMonitor(GitProgressHandler("Downloading appropriate data...", progressHandler))
                    .setRefSpecs("refs/tags/${version.tagName}:refs/tags/${version.tagName}")
                    .call()

                dataRepo.reset()
                    .setProgressMonitor(GitProgressHandler("Installing appropriate data...", progressHandler))
                    .setMode(ResetType.HARD)
                    .setRef("refs/tags/${version.tagName}")
                    .call()
            } catch (e: Exception) {
                warningHandler?.handle("Failed to get data tag corresponding to this release! Falling back to master branch.\n\nPlease report this issue to the OpenSR team. It should still be safe to play this version of OpenSR, but there is a slight possibility that something will go wrong.")
                dataRepo.fetch()
                    .setProgressMonitor(GitProgressHandler("Downloading latest data...", progressHandler))
                    .call()
                dataRepo.reset()
                    .setProgressMonitor(GitProgressHandler("Installing latest data...", progressHandler))
                    .setMode(ResetType.HARD)
                    .setRef("refs/remotes/origin/master")
                    .call()
            }
        }

        progressHandler?.handle("Extracting binaries...")
        ZipFile("opensr-binaries.zip").use { zip ->
            val total = zip.size()
            var extracted = 0
            val lock = ReentrantLock()
            zip.stream().parallel().forEach { entry ->
                if(!entry.isDirectory) {
                    File(Settings.instance.gamePath, entry.name).also { file ->
                        file.parentFile.mkdirs()
                        file.createNewFile()
                        file.outputStream().use {
                            zip.getInputStream(entry).copyTo(it)
                        }
                        lock.withLock {
                            progressHandler?.handle("Extracting binaries... ${++extracted}/$total files extracted")
                        }
                    }
                }
            }
        }
    }
}