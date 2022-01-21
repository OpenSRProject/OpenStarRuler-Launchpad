package io.github.openstarruler.launchpad.view

import javafx.scene.control.ListCell
import org.eclipse.jgit.lib.Ref

// This is something I'd like to use at some point,
// but I'd need to reconsider how some other stuff works, first.
//
// Not going to do that now, but I'm keeping the code here just in case.
class RefCell: ListCell<Ref>() {
    override fun updateItem(item: Ref?, empty: Boolean) {
        super.updateItem(item, empty)

        text = if (item == null) "" else {
            if (item.name.startsWith("refs/remotes/origin/")) "Branch: ${item.name.replaceFirst("refs/remotes/origin/", "")}"
            else if (item.name.startsWith("refs/tags/")) "Tag: ${item.name.replaceFirst("refs/tags/", "")}"
            else "Unexpected ref type: ${item.name}"
        }
    }
}