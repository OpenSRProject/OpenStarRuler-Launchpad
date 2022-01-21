package io.github.openstarruler.launchpad.adapter

import com.google.gson.Gson
import io.github.openstarruler.launchpad.model.Release
import okhttp3.OkHttpClient
import okhttp3.Request

object GitHub {
    val GH_API = "https://api.github.com"

    fun getReleasesUrl(owner: String = "OpenSRProject", repo: String): String {
        return "$GH_API/repos/$owner/$repo/releases"
    }

    fun getReleases(owner: String = "OpenSRProject", repo: String): List<Release> {
        val okHttpClient = OkHttpClient.Builder()
            .build()
        val request = Request.Builder()
            .url(getReleasesUrl(owner, repo))
            .build()
        okHttpClient.newCall(request).execute().body?.charStream().let {
            return try { Gson().fromJson(it!!) }
                catch(e: Exception) { listOf() }
        }
    }
}