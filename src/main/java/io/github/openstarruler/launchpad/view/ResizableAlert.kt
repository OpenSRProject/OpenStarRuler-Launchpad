package io.github.openstarruler.launchpad.view

import javafx.scene.control.Alert

class ResizableAlert(alertType: AlertType?, contentText: String?) : Alert(alertType, contentText) {
    init {
        this.isResizable = true
    }
}