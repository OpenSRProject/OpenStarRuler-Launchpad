package io.github.openstarruler.launchpad.model

import com.google.gson.annotations.SerializedName

data class Asset(
    @SerializedName("browser_download_url") val downloadUrl: String,
    val name: String
)