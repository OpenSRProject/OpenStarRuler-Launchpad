package io.github.openstarruler.launchpad.view

import io.github.openstarruler.launchpad.adapter.Settings
import io.github.openstarruler.launchpad.adapter.Utils
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.layout.GridPane
import javafx.stage.DirectoryChooser
import javafx.stage.Window
import java.io.File
import java.io.IOException

class OpenSRManagerPane : GridPane() {
    lateinit var playButton: Button

    @FXML lateinit var osrVersionList: ListView<String>
    @FXML lateinit var gamePathCaption: Label
    @FXML lateinit var gamePathLabel: Label
    @FXML lateinit var findGameButton: Button
    @FXML lateinit var updateGameButton: Button
    @FXML lateinit var osrVersionInfo: TextArea

    init {
        val fxmlLoader = FXMLLoader(
            javaClass.getResource(
                "OpenSRManager.fxml"
            )
        )
        fxmlLoader.setRoot(this)
        fxmlLoader.setControllerFactory { this }
        fxmlLoader.load<OpenSRManagerPane>()
    }

    @FXML
    fun initialize() {
    }

    @FXML
    fun setSR2Path() {
        setSR2Path(playButton.scene.window)
    }

    private fun setSR2Path(window: Window) {
        playButton.isDisable = true
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
        playButton.isDisable = false
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
