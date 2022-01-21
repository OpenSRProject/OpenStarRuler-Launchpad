package io.github.openstarruler.launchpad.view

import io.github.openstarruler.launchpad.model.Release
import javafx.scene.control.ListCell

class ReleaseCell: ListCell<Release?>() {
    override fun updateItem(item: Release?, empty: Boolean) {
        super.updateItem(item, empty)

        text = if (item == null) "" else item.name
    }
}