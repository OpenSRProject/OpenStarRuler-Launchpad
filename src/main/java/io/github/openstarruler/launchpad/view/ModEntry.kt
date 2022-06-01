package io.github.openstarruler.launchpad.view

import io.github.openstarruler.launchpad.adapter.sr2utils.ManagedMod
import io.github.openstarruler.launchpad.adapter.sr2utils.ModEnumerator
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
    private lateinit var mod: ManagedMod
    private lateinit var controller: MainController

    @FXML
    fun initialize() {

    }

    fun toggleMod() {
        ModEnumerator.toggleMod(mod.ident)
    }

    fun updateCurrentBranch() {

    }

    fun openMod() {

    }

    fun uninstallMod() {

    }

    companion object {
        @Throws(IOException::class)
        fun buildEntry(mod: ManagedMod, controller: MainController): ModEntry {
            val loader = FXMLLoader(ModEntry::class.java.getResource("ModEntry.fxml"))
            val entry: ModEntry = loader.load()
            entry.mod = mod
            entry.controller = controller
            entry.updateButton.isVisible = mod.isGitMod()

            return entry
        }
    }
}
