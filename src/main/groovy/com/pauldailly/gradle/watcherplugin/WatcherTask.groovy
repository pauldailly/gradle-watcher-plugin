package com.pauldailly.gradle.watcherplugin

import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.TaskAction

import java.nio.file.FileSystems

/**
 * Created by paul on 23/03/2014.
 */
class WatcherTask extends DefaultTask {
    NamedDomainObjectContainer<WatchJob> fileWatcherTasks

    @TaskAction
    def watcherAction() {
        if (fileWatcherTasks == null) {
            fileWatcherTasks = project.watch.watchJobs
        }

        new WatcherService(FileSystems.getDefault().newWatchService(), (WatchJob[]) fileWatcherTasks.toArray());
    }
}
