package io.github.openstarruler.launchpad.model

import io.github.openstarruler.launchpad.adapter.sr2utils.DataReader
import org.eclipse.jgit.lib.ObjectLoader
import java.io.File

class Modinfo(val inRoot: Boolean, val folderName: String, val dataReader: DataReader) {
    private var name: String? = null
    internal var description: String? = null
    private var parentName: String? = null
    private var version = 0
    private var compatibility = 0
    private var isBase = false
    private var listed = false
    private var forCurrentVersion = false
    private var overrides: MutableList<String?> = mutableListOf()
    private var overridePatterns: MutableList<List<String?>?> = mutableListOf()
    private var fallbacks: MutableMap<Int, String?> = mutableMapOf()

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
                overrides.add(value)
                val compiled = file.compilePattern(value)
                overridePatterns.add(compiled)
            } else if (key.equals("Derives From", ignoreCase = true)) {
                parentName = if (value == "-") "" else value
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
            }
        }
    }

    companion object {
        private const val CUR_COMPATIBILITY = 200
    }

    init {
        parseModinfo(dataReader)
    }
}
