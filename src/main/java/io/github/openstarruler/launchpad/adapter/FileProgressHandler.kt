package io.github.openstarruler.launchpad.adapter

class FileProgressHandler(private val count: Int, private val handler: TextHandler?) {
    var current = 0

    fun handle(filename: String) {
        current++
        handler?.handle("Copying file $current/$count - $filename")
    }
}