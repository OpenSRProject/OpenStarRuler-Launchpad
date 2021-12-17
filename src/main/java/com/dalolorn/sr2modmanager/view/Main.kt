package com.dalolorn.sr2modmanager.view

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.stage.Stage
import java.io.IOException
import kotlin.system.exitProcess

class Main : Application() {
    override fun start(primaryStage: Stage) {
        try {
            val root = FXMLLoader.load<Parent>(javaClass.getResource("MainFrame.fxml"))
            val scene = Scene(root)
            primaryStage.title = "SR2 Mod Manager"
            primaryStage.scene = scene
            primaryStage.show()
        } catch (e: IOException) {
            val msg = ResizableAlert(Alert.AlertType.ERROR, "Cannot load GUI, application will now terminate.")
            e.printStackTrace()
            msg.showAndWait().ifPresent { exitProcess(-1) }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(*args)
        }
    }
}