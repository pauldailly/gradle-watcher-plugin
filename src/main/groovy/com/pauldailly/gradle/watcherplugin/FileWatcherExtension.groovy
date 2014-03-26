package com.pauldailly.gradle.watcherplugin

import org.gradle.api.NamedDomainObjectContainer

/**
 * Created by paul on 23/03/2014.
 */
class FileWatcherExtension {

    final NamedDomainObjectContainer<WatchJob> watchJobs

    FileWatcherExtension(watchJobs) {
        this.watchJobs = watchJobs
    }

    def watchJobs(Closure closure) {
        watchJobs.configure(closure)
    }

}
