package com.pauldailly.gradle.watcherplugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by paul on 23/03/2014.
 */
class WatcherPlugin implements Plugin<Project> {
    void apply(Project project) {
        // Create and install custom tasks
        project.task('watchFiles', type: WatcherTask) {
            group = 'FileWatcherPlugin'
            description = 'Watches file(s) for changes and runs commands when changes occur'
        }

        // Create and install the extension object
        project.extensions.create("watch",
                FileWatcherExtension,
                project.container(WatchJob))
    }
}
