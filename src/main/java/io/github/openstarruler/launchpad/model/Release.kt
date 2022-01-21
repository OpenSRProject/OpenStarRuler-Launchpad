package io.github.openstarruler.launchpad.model

import com.google.gson.annotations.SerializedName

data class Release(
    @SerializedName("tag_name") val tagName: String,
    val name: String,
    val assets: List<Asset>,
    val body: String
)