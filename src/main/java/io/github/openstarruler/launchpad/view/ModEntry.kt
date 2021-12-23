package io.github.openstarruler.launchpad.view

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.layout.BorderPane
import java.io.IOException

class ModEntry : BorderPane() {
    @FXML private lateinit var enabledToggle: CheckBox
    @FXML private lateinit var updateButton: Button
    @FXML private lateinit var uninstallButton: Button
    @FXML private lateinit var openButton: Button

    @FXML
    fun initialize() {

    }

    fun toggleMod() {

    }

    fun updateCurrentBranch() {

    }

    fun openMod() {

    }

    companion object {
        @Throws(IOException::class)
        fun buildEntry(): ModEntry {
            val loader = FXMLLoader(ModEntry::class.java.getResource("ModEntry.fxml"))
            return loader.load()
        }
    }
}
