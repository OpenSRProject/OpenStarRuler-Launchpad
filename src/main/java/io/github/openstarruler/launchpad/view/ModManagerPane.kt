package io.github.openstarruler.launchpad.view

import io.github.openstarruler.launchpad.adapter.sr2utils.ModEnumerator
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.layout.VBox
import javafx.stage.Stage

class ModManagerPane : ScrollPane() {
    @FXML private lateinit var modList: VBox

    init {
        val fxmlLoader = FXMLLoader(
            javaClass.getResource(
                "ModManager.fxml"
            )
        )
        fxmlLoader.setRoot(this)
        fxmlLoader.setControllerFactory { this }
        fxmlLoader.load<ModManagerPane>()
    }

    fun refreshMods() {
        modList.children.clear()
        MainController.executeTask("Enumerating installed mods...") { taskRefreshStage: Stage ->
            object : Task<Unit>() {
                override fun call() {
                    ModEnumerator.refreshMods(
                        { warning ->
                            Platform.runLater {
                                val msg = ResizableAlert(Alert.AlertType.WARNING, warning)
                                msg.initOwner(taskRefreshStage.scene.window)
                                msg.show()
                            }
                        },
                        { error ->
                            Platform.runLater {
                                val msg = ResizableAlert(Alert.AlertType.ERROR, error)
                                msg.initOwner(taskRefreshStage.scene.window)
                                msg.show()
                            }
                        }
                    ) { message -> updateMessage(message) }
                    updateMessage("Creating controls...")
                    Platform.runLater {
                        ModEnumerator.mods.forEach {
                            modList.children.add(Button("Weird"))
                            modList.children.add(ModEntry.buildEntry(it.value, MainController.controller))
                        }
                    }
                }

                override fun succeeded() {
                    taskRefreshStage.close()
                }
            }
        }
    }

    @FXML
    fun initialize() {
        // Not doing anything right now
    }
}
