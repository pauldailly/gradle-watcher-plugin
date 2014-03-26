package com.pauldailly.gradle.watcherplugin

/**
 * Created by paul on 06/02/2014.
 */
class WatchJob {
    def name
    def taskToRun
    def files

    WatchJob(String name) {
        this.name = name
    }
}
