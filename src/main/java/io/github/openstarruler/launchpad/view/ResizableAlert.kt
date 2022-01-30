package io.github.openstarruler.launchpad.view

import javafx.scene.control.Alert
import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.stage.Window

class ResizableAlert(alertType: AlertType?, contentText: String?) : Alert(alertType, contentText) {
    init {
        isResizable = true
        setIcon(dialogPane.scene.window)
    }

    companion object {
        fun setIcon(window: Window) {
            (window as? Stage)?.icons?.setAll(Image(ResizableAlert::class.java.getResource("/io/github/openstarruler/launchpad/sr2.png").toString()))
        }
    }
}
