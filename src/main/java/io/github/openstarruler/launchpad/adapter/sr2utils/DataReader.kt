package io.github.openstarruler.launchpad.adapter.sr2utils

import org.eclipse.jgit.lib.ObjectLoader
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.stream.Collectors

class DataReader {
    private val filename: String
    private var lines: List<String>? = null
    private var allowLines: Boolean
    private var fullLine = false
    private var allowMultiline = true
    private var skipComments = true
    private var skipEmpty = true
    private var inMultiline = false
    private var squash = false
    private var indent = 0
    private var lineIndex = 0
    var line: String = ""
    var key: String = ""
    var value: String = ""

    constructor(file: File, allowLines: Boolean = true) {
        this.allowLines = allowLines
        filename = file.name
        FileInputStream(file).use { fileStream ->
            BufferedReader(InputStreamReader(fileStream)).use { fileReader ->
                lines = fileReader.lines().collect(Collectors.toUnmodifiableList())
            }
        }
    }

    constructor(fileLoader: ObjectLoader, name: String, allowLines: Boolean = true) {
        this.allowLines = allowLines
        filename = name
        fileLoader.openStream().use { fileStream ->
            BufferedReader(InputStreamReader(fileStream)).use { fileReader ->
                lines = fileReader.lines()
                    .map { str -> "$str\n" }
                    .collect(Collectors.toUnmodifiableList())
            }
        }
    }

    fun position(): String {
        return "$filename | Line $lineIndex"
    }

    fun feed(feedLine: String): Boolean {
        line = feedLine
        return handle()
    }

    private fun handle(): Boolean {
        // Handle multiline values
        if (inMultiline) {
            return handleMultilineValues()
        }

        // Cut off comments
        cutOffComments()

        // Get indent level
        if (indentLevel) return false

        // Detect comments
        if (skipEmpty && line.isEmpty()) return false
        val isKeyValue = splitKeyValue(line)
        return if (!isKeyValue) {
            if (allowLines) {
                fullLine = true
                true
            } else {
                false
            }
        } else {
            key = key.trim { it <= ' ' }
            value = value.trim { it <= ' ' }
            fullLine = false

            // Detect multiline blocks
            if (allowMultiline && (value == "<<" || value == "<<|")) {
                squash = value.length == 3
                value = ""
                inMultiline = true
                return false
            }
            key.isNotEmpty()
        }
    }

    private fun handleMultilineValues(): Boolean {
        line = line.trim { it <= ' ' }
        if (line.length > 1) {
            line = if (line[line.length - 1] == '\\') line.substring(
                0,
                line.length - 1
            ) else if (squash) " " else "\n"
        } else {
            // Preserve empty lines in squash mode
            line += if (squash) "\n\n" else "\n"
        }
        return if (line.startsWith(">>")) {
            // Remove the last linebreak
            if (value.isNotEmpty() && value[value.length - 1] == '\n') value =
                value.substring(0, value.length - 1)
            inMultiline = false
            true
        } else {
            value += line
            false
        }
    }

    private fun cutOffComments() {
        if (skipComments) {
            val commentIndex = line.indexOf("//")
            if (commentIndex != -1) line = line.substring(0, commentIndex)
        }
    }

    private val indentLevel: Boolean
        get() {
            val pos = line.length - line.trimStart().length
            if (pos == 0) {
                if (skipEmpty) return true
                indent = 0
            } else {
                val rpos = line.substring(0, line.length - line.trimEnd().length).length
                if (rpos != 0) line = line.substring(pos, rpos - pos + 1)
                indent = pos
            }
            return false
        }

    operator fun next(): Boolean {
        while (lineIndex < lines!!.size) {
            line = lines!![lineIndex].trim { it <= ' ' }
            lineIndex++
            if (handle()) return true
        }
        return false
    }

    fun reset() {
        lineIndex = 0
    }

    fun splitKeyValue(input: String, separator: Char = ':'): Boolean {
        val index = input.indexOf(separator)
        if (index == -1) return false
        key = input.substring(0, index)
        value = input.substring(index + 1)
        return true
    }

    fun compilePattern(pattern: String): List<String> {
        var pattern = pattern
        pattern = pattern.lowercase()
        if (pattern.isEmpty()) return emptyList()
        val compiled = mutableListOf(*pattern.split("\\*").toTypedArray())
        if (pattern.endsWith("*")) compiled.add("")
        return compiled
    }
}
