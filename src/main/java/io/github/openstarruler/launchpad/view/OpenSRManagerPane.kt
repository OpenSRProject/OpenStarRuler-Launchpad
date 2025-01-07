package io.github.openstarruler.launchpad.view

import io.github.openstarruler.launchpad.adapter.OpenSRManager
import io.github.openstarruler.launchpad.adapter.Settings
import io.github.openstarruler.launchpad.adapter.Utils
import io.github.openstarruler.launchpad.model.Release
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.layout.GridPane
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import javafx.stage.Window
import javafx.util.Callback
import java.io.File
import java.io.IOException

class OpenSRManagerPane : GridPane() {
    lateinit var playLegacyButton: Button
    lateinit var playButton: Button

    @FXML lateinit var osrVersionList: ListView<Release?>
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
        gamePathLabel.text = Settings.instance.gamePath
        osrVersionList.cellFactory = Callback { ReleaseCell() }
        osrVersionList.items.setAll(OpenSRManager.openSRVersions)
        osrVersionList.selectionModel.selectedItemProperty()
            .addListener { _, _, newValue ->
                osrVersionInfo.text = newValue?.body ?: ""
                updateGameButton.isDisable = newValue == null
            }
        updateGameButton.isDisable = false
    }

    @FXML
    fun setSR2Path() {
        setSR2Path(playButton.scene.window)
    }

    private fun setSR2Path(window: Window) {
        playButton.isDisable = true
        playLegacyButton.isDisable = true
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
            val msg =
                ResizableAlert(AlertType.WARNING, "This is not the root directory of a Star Ruler 2 installation! If you aren't trying to upgrade an existing installation, you can disregard this warning.")
            msg.initOwner(scene.window)
            msg.show()
        }
        Settings.instance.gamePath = dir.absolutePath
        gamePathLabel.text = Settings.instance.gamePath
        playButton.isDisable = !launcher.exists()
        playLegacyButton.isDisable = !File(dir, "bin_legacy").exists()
        if (Settings.instance.isFirstRun) {
            val dlg = ResizableAlert(
                AlertType.CONFIRMATION,
                "Do you want to install the latest stable version of OpenSR now? If not, you can install OpenSR later via the 'Manage OpenSR' tab.")
            dlg.dialogPane.buttonTypes.setAll(ButtonType.YES, ButtonType.NO)
            dlg.headerText = "Install OpenSR now?"
            dlg.initOwner(scene.window)
            dlg.showAndWait()
                .filter { it == ButtonType.YES }
                .ifPresent { installOpenSR(launcher.exists()) }
        }
        Settings.instance.isFirstRun = false
        try {
            Settings.instance.save()
        } catch (e: IOException) {
            val msg = ResizableAlert(AlertType.WARNING, "Failed to save config file!")
            msg.initOwner(scene.window)
            msg.show()
            e.printStackTrace()
        }
    }

    @FXML
    private fun installOpenSR() {
        installOpenSR(false)
    }

    private fun installOpenSR(backupLegacyBinaries: Boolean) {
        MainController.executeTask("Preparing to install OpenSR...") {
            taskInstallStage: Stage ->
            object : Task<Unit>() {
                override fun call() {
                    if(backupLegacyBinaries) {
                        updateMessage("Backing up legacy binaries...")
                        val legacyFolder = File(Settings.instance.gamePath, "bin_legacy")
                        val originalFolder = File(Settings.instance.gamePath, "bin")
                        if(!legacyFolder.exists() && originalFolder.exists()) {
                            originalFolder.renameTo(legacyFolder)
                        }
                        playLegacyButton.isDisable = false
                    }

                    updateMessage("Preparing to install OpenSR...")
                    OpenSRManager.installOpenSR(
                        osrVersionList.selectionModel.selectedItem ?: OpenSRManager.openSRVersions.first { it.tagName == "stable" },
                        { warning ->
                            Platform.runLater {
                                val msg = ResizableAlert(AlertType.WARNING, warning)
                                msg.initOwner(scene.window)
                                msg.show()
                            }
                        },
                        { error ->
                            Platform.runLater {
                                val msg = ResizableAlert(AlertType.ERROR, error)
                                msg.initOwner(scene.window)
                                msg.show()
                            }
                        },
                        { message -> updateMessage(message) }
                    )
                }

                override fun succeeded() {
                    taskInstallStage.close()
                    playButton.isDisable = false
                }
            }
        }
    }
}
