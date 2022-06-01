package io.github.openstarruler.launchpad.adapter

import com.google.gson.Gson
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class Recommendations private constructor() {
    private val history: MutableList<String> = mutableListOf(
        "github.com/OpenSRProject/OpenStarRuler-Modpack",
        "github.com/DaloLorn/Rising-Stars",
        "github.com/Skeletonxf/star-ruler-2-mod-ce",
        "github.com/sol-oriens/Shores-of-Infinity",
        "github.com/Vandaria/SR2-Lost-Sector"
    )

    @Throws(IOException::class)
    fun save() {
        val file = File("history.json")
        if (!file.exists()) {
            file.createNewFile()
        }
        FileWriter(file, false).use { writer -> writer.write(Gson().toJson(this)) }
    }

    /**
     * Adds an item to the saved list (if distinct), removes last item if list length above SIZE_LIMIT
     *
     * @param url the url to be added
     */
    @Throws(IOException::class)
    fun addItem(url: String) {
        // Removing it so it gets added to the start of the list again
        with(history) {
            remove(url)
            add(0, url)
            while (size > HISTORY_SIZE_LIMIT) {
                removeAt(HISTORY_SIZE_LIMIT)
            }
        }
        save()
    }

    val recommendationList: List<String>
        get() = history

    companion object {
        var instance = Recommendations()
            private set

        private const val HISTORY_SIZE_LIMIT = 5

        @Throws(IOException::class)
        fun load(): Boolean {
            val file = File("history.json")
            if (!file.exists()) {
                instance.save()
                return false
            } else {
                FileReader(file).use { reader -> instance = Gson().fromJson(reader, Recommendations::class.java) }
            }
            return true
        }
    }
}
