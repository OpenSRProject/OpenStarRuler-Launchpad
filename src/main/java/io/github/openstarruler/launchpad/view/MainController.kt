package io.github.openstarruler.launchpad.view

import io.github.openstarruler.launchpad.adapter.ModInstaller
import io.github.openstarruler.launchpad.adapter.Settings
import io.github.openstarruler.launchpad.adapter.TextHandler
import io.github.openstarruler.launchpad.adapter.Utils
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
import kotlin.system.exitProcess

class MainController {
    // Shared UI components.
    @FXML private lateinit var playButton: Button

    @FXML private lateinit var tabs: TabPane
    @FXML private lateinit var modsTab: Tab
    @FXML private lateinit var installerTab: Tab
    @FXML private lateinit var osrTab: Tab

    @FXML private lateinit var openRepositoryItem: MenuItem
    @FXML private lateinit var deleteRepositoryItem: MenuItem
    @FXML private lateinit var uninstallModItem: MenuItem

    @FXML private lateinit var modsPane: ModManagerPane
    @FXML private lateinit var installerPane: ModInstallerPane
    @FXML private lateinit var osrPane: OpenSRManagerPane

    fun initialize() {
        tabs.selectionModel.selectedItemProperty().addListener { _, _, newTab ->
            deleteRepositoryItem.isDisable = newTab !== installerTab || !ModInstaller.hasRepo()
            uninstallModItem.isDisable = newTab !== installerTab
        }
        osrPane.playButton = playButton
        installerPane.deleteRepositoryItem = deleteRepositoryItem

        if (
            Settings.instance.isFirstRun
        ) {
            val msg = ResizableAlert(
                AlertType.INFORMATION,
                "Welcome to the OpenSR Launchpad!\n\nAs this is your first time running the Launchpad, we probably don't know where you want to install Star Ruler 2 (or where it is already installed).\n\nIf you want to use an existing SR2 installation: Please navigate to the root folder of your SR2 installation, containing the 'data', 'locales', and 'scripts' folders.\n\nIf you want to install a fresh copy of OpenSR instead, please navigate to whatever folder you want to install the game into."
            )
            msg.showAndWait()
            Platform.runLater { setSR2Path() }
        }
        else if (
            !File(Settings.instance.gamePath, "Star Ruler 2.exe").exists()
            && !File(Settings.instance.gamePath, "StarRuler2.sh").exists()
        ) {
            val msg = ResizableAlert(
                AlertType.WARNING,
                "Could not detect Star Ruler 2 launchers! Please navigate to the root folder of your SR2 installation, containing the 'data', 'locales', and 'scripts' folders!"
            )
            msg.showAndWait()
            Platform.runLater { setSR2Path() }
        }
    }

    @FXML
    private fun setSR2Path() {
        osrPane.setSR2Path()
    }

    @FXML
    private fun openRepository() {
        // Get repository location.
        val chooser = DirectoryChooser()
        chooser.initialDirectory = File("repositories/")
        chooser.initialDirectory.mkdirs()
        chooser.title = "Choose a Repository"
        val dir = chooser.showDialog(playButton.scene.window)
            ?: return // Anything on the window would suffice, playButton was just arbitrarily selected.

        deleteRepositoryItem.isDisable = true
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
                    if (!installerPane.getBranches(errorHandler)) return
                    updateMessage("Getting mod description...")
                    if (!installerPane.getDescription(errorHandler)) return
                    Platform.runLater { installerPane.setUrlText("Connected to $repoURL") }
                    return
                }

                override fun succeeded() {
                    deleteRepositoryItem.isDisable = false
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
        if (installerTab.isSelected) {
            installerPane.deleteRepository()
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
        if (installerTab.isSelected) {
            installerPane.uninstallMod()
        }
    }

    companion object {
        fun executeTask(
            initialText: String,
            taskBuilder: (Stage) -> Task<Unit>
        ) {
            // Start preparing the task.
            val wndwWidth = 800.0
            val taskLabel = Label(initialText)
            taskLabel.padding = Insets(0.0, 20.0, 0.0, 0.0)
            taskLabel.maxWidth = Double.MAX_VALUE
            taskLabel.isWrapText = true
            taskLabel.textOverrun = OverrunStyle.CENTER_ELLIPSIS

            val progress = ProgressBar()
            progress.maxWidth = Double.MAX_VALUE

            val taskPane = VBox()
            taskPane.padding = Insets(10.0, 10.0, 30.0, 10.0)
            taskPane.spacing = 5.0
            taskPane.children.addAll(taskLabel, progress)

            val taskStage = Stage(StageStyle.UTILITY)
            taskStage.initModality(Modality.APPLICATION_MODAL)
            taskStage.isResizable = true
            taskStage.scene = Scene(taskPane)
            taskStage.width = wndwWidth
            taskStage.show()

            val task = taskBuilder(taskStage)
            progress.progressProperty().bind(task.progressProperty())
            taskLabel.textProperty().bind(task.messageProperty())
            taskStage.show()
            Thread(task).start()
        }
    }
}
