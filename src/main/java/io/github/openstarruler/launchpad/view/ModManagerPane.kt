package io.github.openstarruler.launchpad.view

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.ScrollPane

class ModManagerPane : ScrollPane() {
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

    @FXML
    fun initialize() {
        // TODO: Implement this.
    }
}