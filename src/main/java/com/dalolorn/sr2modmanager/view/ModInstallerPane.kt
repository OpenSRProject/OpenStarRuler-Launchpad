package com.dalolorn.sr2modmanager.view

import com.dalolorn.sr2modmanager.adapter.ModInstaller
import com.dalolorn.sr2modmanager.adapter.ModInstaller.TextHandler
import com.dalolorn.sr2modmanager.adapter.Recommendations
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import java.io.IOException

class ModInstallerPane : GridPane() {
    @FXML var connectButton: Button? = null
    @FXML var installButton: Button? = null

    @FXML var urlField: TextField? = null
    @FXML var urlLabel: Label? = null
    @FXML var recommendationLabel: Label? = null

    @FXML var modInfo: TextArea? = null
    @FXML var branchInfo: TextArea? = null

    @FXML var branchList: ListView<String>? = null
    @FXML var modList: ListView<String>? = null

    @FXML var recommendationList: ListView<String>? = null

    @FXML
    fun initialize() {
        branchList!!.selectionModel.selectedItemProperty()
            .addListener { _, _, newValue ->
                setActiveBranch(newValue)
            }
        recommendationList!!.selectionModel.selectedIndexProperty()
            .addListener { _, _, newValue ->
                urlField!!.text = if (newValue.toInt() >= 0) recommendationList!!.items[newValue.toInt()] else ""
            }
        try {
            Recommendations.load()
            recommendationList!!.items.addAll(Recommendations.instance.recommendationList)
        } catch (e: IOException) {
            val msg = Alert(AlertType.WARNING, "Could not load history.json! Cause:$e")
            e.printStackTrace()
            msg.showAndWait()
        }
    }

    private fun setActiveBranch(branchName: String) {
        branchInfo!!.text = ModInstaller.setActiveBranch(branchName)
        installButton!!.isDisable = !ModInstaller.hasBranch()
    }

    @FXML
    private fun connectToRepository() {
        MainController.executeTask(
            "Loading repository..."
        ) { taskUpdateStage: Stage ->
            object : Task<Unit>() {
                override fun call() {
                    var repoURL: String
                    val errorHandler = TextHandler {
                        Platform.runLater {
                            val msg = ResizableAlert(AlertType.ERROR, it)
                            msg.show()
                        }
                    }

                    if (ModInstaller.connectToRepository(
                            urlField!!.text,
                            { updateMessage(it) },
                            errorHandler
                        ).also { repoURL = it!! } == null
                    ) return

                    updateMessage("Fetching branch list...")
                    if (!getBranches(errorHandler)) return

                    updateMessage("Getting mod description...")
                    if (!getDescription(errorHandler)) return

                    Platform.runLater {
                        setUrlText("Connected to $repoURL")
                        try {
                            Recommendations.instance.addItem(urlField!!.text)
                            recommendationList!!.items.setAll(Recommendations.instance.recommendationList)
                        } catch (e: IOException) {
                            val msg = Alert(AlertType.WARNING, "Could not save history.json! Cause: $e")
                            e.printStackTrace()
                            msg.showAndWait()
                        }
                    }
                }

                override fun succeeded() {
                    taskUpdateStage.close()
                }
            }
        }
    }

    @FXML
    private fun installMod() {
        MainController.executeTask(
            "Preparing to install mod..."
        ) { taskInstallStage: Stage ->
            object : Task<Unit>() {
                override fun call() {
                    updateMessage("Preparing to install mod...")
                    ModInstaller.installMod(
                        { warning ->
                            Platform.runLater {
                                val msg: Alert = ResizableAlert(AlertType.WARNING, warning)
                                msg.show()
                            }
                        },
                        { message -> updateMessage(message) },
                        { info ->
                            Platform.runLater {
                                val msg: Alert = ResizableAlert(AlertType.INFORMATION, info)
                                msg.show()
                            }
                        },
                        { error ->
                            Platform.runLater {
                                val msg: Alert = ResizableAlert(AlertType.ERROR, error)
                                msg.show()
                            }
                        }
                    )
                    return
                }

                override fun succeeded() {
                    taskInstallStage.close()
                }
            }
        }
    }

    fun getBranches(errorHandler: TextHandler?): Boolean {
        val tagNames = ModInstaller.getBranches(errorHandler) ?: return false
        Platform.runLater {
            branchList!!.items.clear()
            branchList!!.items.addAll(tagNames)
        }
        return true
    }

    fun getDescription(errorHandler: TextHandler?): Boolean {
        val description = ModInstaller.getDescription(errorHandler) ?: return false
        Platform.runLater {
            modInfo!!.text = description
            branchInfo!!.text = "No branches selected"
        }
        return true
    }

    fun setUrlText(url: String?) {
        urlLabel!!.text = url
    }

    fun deleteRepository() {
        val dialog = ResizableAlert(
            AlertType.CONFIRMATION,
            "Are you sure you want to delete this repository from your computer? You will not be able to access it again until you redownload it."
        )
        val dlgButtons = dialog.dialogPane.buttonTypes
        dlgButtons.clear()
        dlgButtons.add(ButtonType.YES)
        dlgButtons.add(ButtonType.NO)
        dialog.headerText = "Delete Repository?"
        dialog.showAndWait()
            .filter { response: ButtonType -> response == ButtonType.YES }
            .ifPresent {
                if (ModInstaller.deleteRepository { error: String? ->
                        val msg = ResizableAlert(AlertType.ERROR, error)
                        msg.show()
                    }) {
                    branchList!!.items.clear()
                    modInfo!!.text = ""
                    branchInfo!!.text = ""
                }
            }
    }

    fun uninstallMod() {
        val dialog = ResizableAlert(
            AlertType.CONFIRMATION,
            "Are you sure you want to uninstall this mod? The associated repository will not be removed from your computer."
        )
        val dlgButtons = dialog.dialogPane.buttonTypes
        dlgButtons.clear()
        dlgButtons.add(ButtonType.YES)
        dlgButtons.add(ButtonType.NO)
        dialog.headerText = "Uninstall Mod?"
        dialog.showAndWait()
            .filter { response: ButtonType -> response == ButtonType.YES }
            .ifPresent {
                ModInstaller.uninstallMod(
                    { error: String? ->
                        val msg = ResizableAlert(AlertType.ERROR, error)
                        msg.show()
                    },
                    modList!!.selectionModel.selectedItem
                )
            }
    }
}