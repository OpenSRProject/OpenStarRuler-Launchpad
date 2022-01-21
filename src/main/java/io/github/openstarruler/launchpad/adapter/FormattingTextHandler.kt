package io.github.openstarruler.launchpad.adapter

class FormattingTextHandler(private val pattern: String, private val handler: TextHandler?): TextHandler {
    override fun handle(text: String) {
        handler?.handle(String.format(pattern, text))
    }
}
