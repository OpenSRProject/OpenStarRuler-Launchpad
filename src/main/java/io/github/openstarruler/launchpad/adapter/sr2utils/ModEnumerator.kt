package io.github.openstarruler.launchpad.adapter.sr2utils

import io.github.openstarruler.launchpad.adapter.Settings
import io.github.openstarruler.launchpad.adapter.TextHandler
import io.github.openstarruler.launchpad.adapter.Utils
import io.github.openstarruler.launchpad.model.Modinfo
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

object ModEnumerator {
    val profileRoot: Path
        get() = if (Utils.IS_WINDOWS) {
            // %USERPROFILE%/Documents/My Games/Star Ruler 2/mods
            Path(System.getProperty("user.home")) / "Documents" / "My Games" / "Star Ruler 2"
        } else Path(System.getProperty("user.home")) / ".starruler2"

    private val modLocations: List<Path>
    get() = listOf(
        Path(Settings.instance.gamePath) / "mods",
        profileRoot / "mods",
        Path(Settings.instance.gamePath) / "../../workshop/content/282590"
    )

    enum class ModStatus {
        DISABLED,
        IMPLIED, // Dependency of an active mod, otherwise disabled
        ENABLED_DEPENDENCY, // Dependency of an active mod, also enabled
        FORCED, // Enabled despite compatibility warnings
        ENABLED
    }

    val mods: MutableMap<String, ManagedMod> = mutableMapOf()
    val modExclusions: MutableMap<String, MutableList<String>> = mutableMapOf()
    val modStatuses: MutableMap<String, ModStatus> = mutableMapOf()

    fun refreshMods(warningHandler: TextHandler?, errorHandler: TextHandler?, progressHandler: TextHandler?) {
        mods.clear()
        modExclusions.clear()
        modStatuses.clear()
        try {
            for (location in modLocations) {
                if(!location.toFile().exists())
                    continue
                val modList = Files.newDirectoryStream(location) {
                    it.isDirectory() && it.resolve("modinfo.txt").toFile().exists()
                }
                for (modFolder in modList) {
                    progressHandler?.handle("Processing mod $modFolder...")
                    val mod = modFolder.resolve("modinfo.txt")
                    var ident = modFolder.name
                    val modinfo = Modinfo(false, ident, mod.toFile())
                    val issues: MutableList<String> = mutableListOf()
                    val files: Set<String> =
                        buildSet { Files.walk(modFolder).filter { !it.contains(Path(".git/")) }.filter { it.parent != modFolder }.filter(Files::isRegularFile).forEach { add(modFolder.relativize(it).toString()) } }

                    ident = validateMod(modinfo, ident, issues)
                    val managedMod =
                        ManagedMod(Modinfo(false, ident, mod.toFile()), ident, modFolder, files, issues.toList())

                    mods[ident] = managedMod
                    progressHandler?.handle("Caching compatibility data for ${modinfo.name}...")
                    modStatuses[ident] = ModStatus.DISABLED
                    for (otherMod in mods.values)
                        if (!managedMod.isCompatibleWith(otherMod)) {
                            val exclusions = modExclusions.getOrPut(ident) { mutableListOf() }
                            exclusions += otherMod.ident
                            modExclusions[otherMod.ident]!! += ident
                        }
                }
            }

            progressHandler?.handle("Reading mod statuses")
            val modStatusFile = (profileRoot / "mods.txt").readLines()
            var key: String
            var value: String
            var index: Int
            for (line in modStatusFile) {
                index = line.indexOf(':')
                if (index == -1) continue
                key = line.substring(0, index)
                value = line.substring(index + 1)

                if (value != "Disabled" && value != "Implied")
                    enableMod(key, value == "Forced", false)
            }
        } catch (e: Exception) {
            errorHandler?.handle("Failed to refresh mod list! Reason: $e")
        }
    }

    fun toggleMod(ident: String) {
        when(modStatuses[ident]) {
            ModStatus.DISABLED -> enableMod(ident, null, true)
            else -> disableMod(ident)
        }
    }

    fun enableMod(ident: String, isForced: Boolean?, shouldSync: Boolean = true) {
        // Check compatibility before enabling!
        if (modExclusions[ident] != null && mods.any { modExclusions[ident]!!.contains(it.key) && modStatuses[ident] != ModStatus.DISABLED })
            return
        if (mods[ident] == null)
            return
        val forceEnable = isForced == true || mods[ident]!!.modinfo.forCurrentVersion

        modStatuses[ident] = if(forceEnable) ModStatus.FORCED else ModStatus.ENABLED
        var ancestor = mods[ident]?.getImmediateAncestor()
        while(ancestor != null) {
            if(modStatuses[ancestor.ident] == ModStatus.DISABLED)
                modStatuses[ancestor.ident] = ModStatus.IMPLIED
            ancestor = ancestor.getImmediateAncestor()
        }

        if(shouldSync)
            syncModStatuses()
    }

    fun disableMod(ident: String) {
        if(modStatuses[ident] == ModStatus.ENABLED_DEPENDENCY) {
            modStatuses[ident] = ModStatus.IMPLIED
            syncModStatuses()
            return
        }
        if(modStatuses[ident] == ModStatus.IMPLIED)
            return

        modStatuses[ident] = ModStatus.DISABLED
        var ancestor = mods[ident]?.getImmediateAncestor()
        while(ancestor != null && modStatuses[ancestor.ident] == ModStatus.IMPLIED) {
            if(mods.all { modStatuses[it.key]!! == ModStatus.DISABLED || !ancestor!!.isAncestorOf(it.value) } )
                modStatuses[ancestor.ident] = ModStatus.DISABLED
            ancestor = ancestor.getImmediateAncestor()
        }

        syncModStatuses()
    }

    fun syncModStatuses() {
        val statuses: MutableList<String> = mutableListOf()
        for ((ident, status) in modStatuses) {
            statuses += "$ident: ${when(status) {
                ModStatus.FORCED -> "Forced"
                ModStatus.ENABLED -> "Enabled"
                ModStatus.ENABLED_DEPENDENCY -> "Implied"// Per game/mods/mod_manager:349-350, unrecognized values are treated as "Disabled".
                else -> "Disabled"
            }}"
        }
        (profileRoot / "mods.txt").writeLines(statuses)
    }

    private fun validateMod(modinfo: Modinfo, ident: String, issues: MutableList<String>): String {
        if(modinfo.parentName != null && modinfo.overrides.isNotEmpty())
            issues += "WARNING: Mod '${modinfo.name}' has overrides declared, but does not derive from any mod."
        while(mods.any { it.value.modinfo.name == modinfo.name }) {
            issues += "Duplicate mod named '${modinfo.name}'"
            modinfo.name += " (Copy)"
        }
        var updatedIdent = ident
        while(mods.containsKey(updatedIdent)) {
            issues += "Duplicate mod ident '${ident}'"
            updatedIdent += " (Copy)"
        }
        return updatedIdent;
    }

    /* TODO: Figure out a registerMod(Path) method which adds/updates a mod without
        requiring a full refresh of the mod list. My first attempt caused multiple breaches
        in data consistency.
        As far as I can tell, this method has the following requirements:
            - Adds a new/updated mod to `mods`. Easy enough.
            - Registers the mod's exclusions in `modExclusions`. Simple.
            - If other mods derive from the new mod, update mod exclusions to account for parental chains. This one's tricky.
            - Update duplicate names/idents such that data consistency is maintained throughout subsequent calls to `refreshMods()`. Yikes.
            - If updating to a mod with a new name, we may have to *remove* (Copy) suffixes, not just add them. Double yikes.
        ... This might not happen for a good while.
     */
}