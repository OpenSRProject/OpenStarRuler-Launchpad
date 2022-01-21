package io.github.openstarruler.launchpad.adapter

import org.eclipse.jgit.lib.ProgressMonitor

class GitProgressHandler(val task: String, val handler: TextHandler?): ProgressMonitor {
    var taskTitle = ""
    var tasksDone = 0
    var taskCount = 0

    override fun start(totalTasks: Int) = Unit

    override fun beginTask(title: String?, totalWork: Int) {
        taskTitle = title ?: ""
        tasksDone = 0
        taskCount = totalWork
        handler?.handle("$task $taskTitle $tasksDone/$taskCount")
    }

    override fun update(completed: Int) {
        tasksDone++
        handler?.handle("$task $taskTitle $tasksDone/$taskCount")
    }

    override fun endTask() = Unit

    override fun isCancelled(): Boolean = false
}