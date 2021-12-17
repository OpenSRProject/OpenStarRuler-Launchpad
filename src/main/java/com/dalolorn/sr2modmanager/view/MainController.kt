package com.dalolorn.sr2modmanager.view

import com.dalolorn.sr2modmanager.adapter.ModInstaller
import com.dalolorn.sr2modmanager.adapter.ModInstaller.TextHandler
import com.dalolorn.sr2modmanager.adapter.Settings
import com.dalolorn.sr2modmanager.adapter.Utils
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

class MainController {
    // Shared UI components.
    @FXML private val playButton: Button? = null

    @FXML private val tabs: TabPane? = null
    @FXML private val modsTab: Tab? = null
    @FXML private val installerTab: Tab? = null
    @FXML private val osrTab: Tab? = null

    @FXML private val openRepositoryItem: MenuItem? = null
    @FXML private val deleteRepositoryItem: MenuItem? = null
    @FXML private val uninstallModItem: MenuItem? = null

    @FXML private val modsPane: ModManagerPane? = null
    @FXML private val installerPane: ModInstallerPane? = null
    @FXML private val osrPane: OpenSRManagerPane? = null

    fun initialize() {
        tabs!!.selectionModel.selectedItemProperty().addListener { _, _, newTab ->
            deleteRepositoryItem!!.isDisable = newTab !== installerTab || !ModInstaller.hasRepo()
            uninstallModItem!!.isDisable = newTab !== installerTab
        }
        osrPane!!.playButton = playButton

        var needsConfig = true
        try {
            needsConfig = !Settings.load()
        } catch (e: IOException) {
            val msg: Alert = ResizableAlert(AlertType.ERROR, "Could not load config.json! Cause: $e")
            e.printStackTrace()
            msg.showAndWait()
        }

        if (
            needsConfig
            || !File(Settings.instance.gamePath, "Star Ruler 2.exe").exists()
            && !File(Settings.instance.gamePath, "StarRuler2.sh").exists()
        ) {
            val msg = ResizableAlert(
                AlertType.WARNING,
                "Could not detect Star Ruler 2 launchers! Please navigate to the root folder of your SR2 installation, containing the files 'Star Ruler 2.exe' and/or 'StarRuler2.sh'!"
            )
            msg.showAndWait()
            setSR2Path()
        }
    }

    @FXML
    private fun setSR2Path() {
        osrPane!!.setSR2Path()
    }

    @FXML
    private fun openRepository() {
        // Get repository location.
        val chooser = DirectoryChooser()
        chooser.initialDirectory = File("repositories/")
        chooser.initialDirectory.mkdirs()
        chooser.title = "Choose a Repository"
        val dir = chooser.showDialog(playButton!!.scene.window)
            ?: return // Anything on the window would suffice, playButton was just arbitrarily selected.

        executeTask("Loading repository...") { taskUpdateStage: Stage ->
            object : Task<Unit>() {
                override fun call() {
                    var repoURL: String
                    val errorHandler = TextHandler { text ->
                        Platform.runLater {
                            val msg: Alert = ResizableAlert(AlertType.ERROR, text)
                            msg.show()
                        }
                    }
                    if (ModInstaller.openRepository(
                            dir,
                            errorHandler
                        ).also { repoURL = it!! } == null
                    ) return
                    updateMessage("Fetching branch list...")
                    if (!installerPane!!.getBranches(errorHandler)) return
                    updateMessage("Getting mod description...")
                    if (!installerPane.getDescription(errorHandler)) return
                    Platform.runLater { installerPane.setUrlText("Connected to $repoURL") }
                    return
                }

                override fun succeeded() {
                    taskUpdateStage.close()
                }
            }
        }
    }

    @FXML
    private fun close() {
        val dialog = ResizableAlert(AlertType.CONFIRMATION, "Quit OpenSR Launchpad?")
        val dlgButtons = dialog.dialogPane.buttonTypes
        dlgButtons.clear()
        dlgButtons.add(ButtonType.YES)
        dlgButtons.add(ButtonType.NO)
        dialog.headerText = "Quit OpenSR Launchpad"
        dialog.showAndWait()
            .filter { response: ButtonType -> response == ButtonType.YES }
            .ifPresent { exitProcess(0) }
    }

    @FXML
    private fun deleteRepository() {
        if (installerTab!!.isSelected) {
            installerPane!!.deleteRepository()
        }
    }

    @FXML
    private fun about() {
        val msg: Alert = ResizableAlert(
            AlertType.INFORMATION,
            """
                  OpenSR Launchpad
                  
                  Version: 2.0.0
                  
                  GitHub: https://github.com/OpenSRProject/OpenStarRuler-Launchpad
                  Discord: https://discord.gg/sUJKJDc
                  """.trimIndent()
        )
        msg.headerText = "About OpenSR Launchpad"
        msg.show()
    }

    @FXML
    private fun startGame() {
        val launcher =
            File(Settings.instance.gamePath, if (Utils.IS_WINDOWS) "Star Ruler 2.exe" else "StarRuler2.sh")
        if (!launcher.exists()) ResizableAlert(
            AlertType.ERROR,
            "Cannot start Star Ruler 2: Couldn't find platform-appropriate loader!"
        )
            .showAndWait()
        if (!launcher.canExecute()) ResizableAlert(
            AlertType.ERROR,
            "Cannot start Star Ruler 2: Couldn't run game loader!"
        )
            .showAndWait()
        try {
            ProcessBuilder(launcher.absolutePath)
                .directory(launcher.parentFile)
                .start()
        } catch (e: Exception) {
            val msg = ResizableAlert(AlertType.ERROR, String.format("Cannot start Star Ruler 2: %s", e))
            e.printStackTrace()
            msg.showAndWait()
        }
    }

    @FXML
    fun uninstallMod() {
        if (installerTab!!.isSelected) {
            installerPane!!.uninstallMod()
        }
    }

    companion object {
        fun executeTask(
            initialText: String,
            taskBuilder: (Stage) -> Task<Unit>
        ) {
            // Start preparing the task.
            val wndwWidth = 400.0
            val taskLabel = Label(initialText)
            taskLabel.prefWidth = wndwWidth - 20
            taskLabel.isWrapText = true

            val progress = ProgressBar()
            progress.prefWidth = wndwWidth

            val taskPane = VBox()
            taskPane.padding = Insets(10.0, 10.0, 30.0, 10.0)
            taskPane.spacing = 5.0
            taskPane.children.addAll(taskLabel, progress)

            val taskStage = Stage(StageStyle.UTILITY)
            taskStage.initModality(Modality.APPLICATION_MODAL)
            taskStage.scene = Scene(taskPane)
            taskStage.show()

            val task = taskBuilder(taskStage)
            progress.progressProperty().bind(task.progressProperty())
            taskLabel.textProperty().bind(task.messageProperty())
            taskStage.show()
            Thread(task).start()
        }
    }
}