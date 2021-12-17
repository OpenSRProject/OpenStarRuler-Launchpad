package com.dalolorn.sr2modmanager.view

import com.dalolorn.sr2modmanager.adapter.Settings
import com.dalolorn.sr2modmanager.adapter.Utils
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.layout.GridPane
import javafx.stage.DirectoryChooser
import javafx.stage.Window
import java.io.File
import java.io.IOException

class OpenSRManagerPane : GridPane() {
    var playButton: Button? = null

    @FXML var osrVersionList: ListView<String>? = null
    @FXML var gamePathCaption: Label? = null
    @FXML var gamePathLabel: Label? = null
    @FXML var findGameButton: Button? = null
    @FXML var updateGameButton: Button? = null
    @FXML var osrVersionInfo: TextArea? = null

    @FXML
    fun initialize() {
    }

    @FXML
    fun setSR2Path() {
        setSR2Path(scene.window)
    }

    private fun setSR2Path(window: Window) {
        playButton!!.isDisable = true
        setSR2Path(File(Settings.instance.gamePath), window)
    }

    private fun setSR2Path(initialDirectory: File, window: Window) {
        val chooser = DirectoryChooser()
        chooser.initialDirectory = initialDirectory
        if (!chooser.initialDirectory.exists()) chooser.initialDirectory = File(".")
        chooser.title = "Set SR2 Path"
        val dir = chooser.showDialog(window)
        if (dir == null || !dir.exists()) return
        val launcher = File(dir, if (Utils.IS_WINDOWS) "Star Ruler 2.exe" else "StarRuler2.sh")
        if (!launcher.exists()) {
            val msg: Alert =
                ResizableAlert(AlertType.ERROR, "This is not the root directory of a Star Ruler 2 installation!")
            msg.showAndWait()
            setSR2Path(dir, window)
            return
        }
        Settings.instance.gamePath = dir.absolutePath
        playButton!!.isDisable = false
        try {
            Settings.instance.save()
        } catch (e: IOException) {
            val msg: Alert = ResizableAlert(AlertType.WARNING, "Failed to save config file!")
            msg.show()
            e.printStackTrace()
        }
    }

    fun installOpenSR() {}
}