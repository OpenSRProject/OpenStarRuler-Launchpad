package com.dalolorn.sr2modmanager.view

import javafx.scene.control.Alert

class ResizableAlert(alertType: AlertType?, contentText: String?) : Alert(alertType, contentText) {
    init {
        this.isResizable = true
    }
}