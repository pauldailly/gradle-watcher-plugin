# Gradle Watcher Plugin

[![Build Status](https://travis-ci.org/pauldailly/gradle-watcher-plugin.svg?branch=master)](https://travis-ci.org/pauldailly/gradle-watcher-plugin)

A Gradle plugin which watches files/directories for changes and takes actions when changes occur.

## Usage

Add the following to your build.gradle in order to use the plugin:

    apply plugin: 'gradleWatcherPlugin'

Then configure the list of files/directories you wish the plugin to monitor and what you want to happen when something changes

    watch {
        watchJobs {
            // Declare each watch job with a unique name e.g 'less' for watching less files (can be any name you want)
            less {
                // Specify which files/directories should be monitored using the gradle filetree method
                files = fileTree(dir: "${project.rootDir}/public/stylesheets", include: '*.less')
                // specify the shell command to execute if any changes are detected in your files
                taskToRun = "lessc --verbose --source-map-url=style.map --source-map=${project.rootDir}/public/stylesheets/style.map ${project.rootDir}/public/stylesheets/style.less ${project.rootDir}/public/stylesheets/style.css"
            },
            js {
                files = fileTree(dir: "${project.rootDir}/public/js", include: '**/*.js')
                taskToRun = ...
            },
            someOtherJob {
                files = fileTree(...),
                taskToRun = ...
            }
        }
    }