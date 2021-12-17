package com.dalolorn.sr2modmanager.adapter

import com.google.gson.Gson
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class Settings private constructor() {
    var gamePath = ".."
    private var installToUser = false
    var isFirstRun = true

    // %USERPROFILE%/Documents/My Games/Star Ruler 2/mods
    val modsFolder: String
        get() = if (installToUser) {
            if (Utils.IS_WINDOWS) {
                // %USERPROFILE%/Documents/My Games/Star Ruler 2/mods
                System.getProperty("user.home") + File.separator + "Documents" + File.separator + "My Games" + File.separator + "Star Ruler 2" + File.separator + "mods"
            } else System.getProperty("user.home") + File.separator + ".starruler2" + File.separator + "mods"
        } else gamePath + File.separator + "mods"

    @Throws(IOException::class)
    fun save() {
        val file = File("config.json")
        if (!file.exists()) {
            file.createNewFile()
        }
        FileWriter(file, false).use { writer -> writer.write(Gson().toJson(this)) }
    }

    companion object {
        var instance = Settings()
            private set

        @Throws(IOException::class)
        fun load(): Boolean {
            val file = File("config.json")
            if (!file.exists()) {
                instance.save()
                return false
            } else {
                FileReader(file).use { reader -> instance = Gson().fromJson(reader, Settings::class.java) }
            }
            return true
        }
    }
}