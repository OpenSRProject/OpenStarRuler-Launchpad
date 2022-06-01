package io.github.openstarruler.launchpad.adapter

import com.google.gson.Gson
import io.github.openstarruler.launchpad.adapter.sr2utils.ModEnumerator
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div

class Settings private constructor() {
    var gamePath = ".."
    private var installToUser = false
    var isFirstRun = true

    // %USERPROFILE%/Documents/My Games/Star Ruler 2/mods
    val modsFolder: Path
        get() = if (installToUser) {
            ModEnumerator.profileRoot / "mods"
        } else Path(gamePath) / "mods"

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
