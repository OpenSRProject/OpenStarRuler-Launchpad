package io.github.openstarruler.launchpad.adapter.sr2utils

import io.github.openstarruler.launchpad.model.Modinfo
import java.nio.file.Path

class ManagedMod(val modinfo: Modinfo, val ident: String, val modFolder: Path, val files: Set<String>, val issues: List<String>) {

    fun isAncestorOf(other: ManagedMod): Boolean {
        var child: ManagedMod? = other
        while (child != null) {
            child = child.getImmediateAncestor()
            if (child == this)
                return true
        }
        return false;
    }

    fun getImmediateAncestor(): ManagedMod? {
        var ancestor: ManagedMod? = null
        if (modinfo.parentName != null)
            ancestor = ModEnumerator.mods.firstNotNullOfOrNull {
                if (it.value.modinfo.name == modinfo.parentName || it.value.ident == modinfo.parentName) it.value else null
            }

        if (ancestor == this) // This is *hopefully* just a superfluous sanity check...
            ancestor = null
        return ancestor
    }

    fun isCompatibleWith(other: ManagedMod): Boolean {
        if(modinfo.isBase && other.modinfo.isBase)
            return false
        else if(isAncestorOf(other) || other.isAncestorOf(this))
            return true
        else return files.none { other.files.contains(it) }
    }

    fun isGitMod(): Boolean {
        return modinfo.repository != null && modinfo.branch != null
    }
}