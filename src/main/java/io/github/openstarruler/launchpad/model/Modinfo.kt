package io.github.openstarruler.launchpad.model

import io.github.openstarruler.launchpad.adapter.sr2utils.DataReader
import org.eclipse.jgit.lib.ObjectLoader
import java.io.File

class Modinfo(val inRoot: Boolean, val folderName: String, val dataReader: DataReader) {
    internal var name: String? = null
    internal var description: String? = null
    internal var parentName: String? = null
    private var version = 0
    internal var compatibility = 0
    internal var isBase = false
    private var listed = false
    internal var forCurrentVersion = false
    internal var overrides: MutableList<String?> = mutableListOf()
    private var overridePatterns: MutableList<List<String?>?> = mutableListOf()
    private var fallbacks: MutableMap<Int, String?> = mutableMapOf()
    internal var repository: String? = null
    internal var branch: String? = null

    constructor(inRoot: Boolean, folderName: String, file: File) : this(inRoot, folderName, dataReader = DataReader(file))

    constructor(inRoot: Boolean, folderName: String, loader: ObjectLoader): this(inRoot, folderName, dataReader = DataReader(loader, "${folderName}modinfo.txt"))

    private fun parseModinfo(file: DataReader) {
        var key: String
        var value: String
        while (file.next()) {
            key = file.key
            value = file.value
            if (key.equals("Name", ignoreCase = true)) {
                name = value
            } else if (key.equals("Description", ignoreCase = true)) {
                description = value
            } else if (key.equals("Override", ignoreCase = true)) {
                overrides += value
                val compiled = file.compilePattern(value)
                overridePatterns += compiled
            } else if (key.equals("Derives From", ignoreCase = true)) {
                parentName = if (value == "-") null else value
            } else if (key.equals("Base Mod", ignoreCase = true)) {
                isBase = java.lang.Boolean.parseBoolean(value)
            } else if (key.equals("Listed", ignoreCase = true)) {
                listed = java.lang.Boolean.parseBoolean(value)
            } else if (key.equals("Version", ignoreCase = true)) {
                version = value.toInt()
            } else if (key.equals("Compatibility", ignoreCase = true)) {
                compatibility = value.toInt()
                forCurrentVersion = compatibility >= CUR_COMPATIBILITY
            } else if (key.equals("Fallback", ignoreCase = true)) {
                file.splitKeyValue(value, '=')
                fallbacks[file.value.toInt()] = file.key
            } else if (key.equals("Repository", ignoreCase = true)) {
                repository = value
            } else if (key.equals("Branch", ignoreCase = true)) {
                branch = value
            }
        }
    }

    companion object {
        const val CUR_COMPATIBILITY = 200
    }

    init {
        parseModinfo(dataReader)
    }
}
