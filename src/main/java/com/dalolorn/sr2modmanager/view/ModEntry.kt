package com.dalolorn.sr2modmanager.view

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.layout.BorderPane
import java.io.IOException

class ModEntry : BorderPane() {
    @FXML private val enabledToggle: CheckBox? = null
    @FXML private val updateButton: Button? = null
    @FXML private val uninstallButton: Button? = null
    @FXML private val openButton: Button? = null

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