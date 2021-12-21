package io.github.openstarruler.launchpad.adapter

import com.google.gson.Gson
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class Recommendations private constructor() {
    private val history: MutableList<String> = mutableListOf()

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
        history.remove(url)
        history.add(0, url)
        while (history.size > HISTORY_SIZE_LIMIT) {
            history.removeAt(HISTORY_SIZE_LIMIT)
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