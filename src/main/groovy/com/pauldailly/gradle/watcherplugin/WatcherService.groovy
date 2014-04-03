package com.pauldailly.gradle.watcherplugin

import java.nio.file.*
import java.util.concurrent.TimeUnit

import static java.nio.file.StandardWatchEventKinds.*

/**
 * Created by paul on 06/02/2014.
 */
class WatcherService {

    protected Map<String, Set<String>> watchTasks;
    protected Map<String, String> commandMap;
    private WatchService watchService;

    public WatcherService(WatchService watchService, WatchJob[] fileWatcherJobs, boolean watchForever = true) {
        this.watchTasks = new HashMap<>()
        this.commandMap = new HashMap<>()
        this.watchService = watchService

        fileWatcherJobs.each { fileWatcherJob ->
            registerFilesToWatch(fileWatcherJob)
        }

        if (watchForever) {
            watchFilesForever()
        }
    }

    private void watchFilesForever() {
        boolean notDone = true;
        while (notDone) {
            try {
                WatchKey key = watchService.poll(200, TimeUnit.MICROSECONDS);
                if (key != null) {
                    List<WatchEvent.Kind<?>> events = key.pollEvents();
                    for (WatchEvent event : events) {

                        WatchEvent<Path> ev = (WatchEvent<Path>) event;

                        def fileModified = ((Path) ev.context()).getFileName().toString()
                        def absoluteFilePath = ((Path) key.watchable()).toAbsolutePath().toString() + "/${fileModified}"
                        def filesToWatch = watchTasks.get(key)

                        if (filesToWatch?.contains(absoluteFilePath)) {

                            executeCommand(key, absoluteFilePath)

                        }
                    }

                    if (!key.reset()) {
                        'The key could not be reset'
                    }

                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void executeCommand(WatchKey key, absoluteFilePath) {
        def commandToRun = commandMap.get(key)
        println "File \"${absoluteFilePath}\" changed"
        println "Running command: \"${commandToRun}\""
        def result = commandToRun.execute().waitFor()
        println "Result: ${result}"
    }

    private List registerFilesToWatch(WatchJob watchJob) {
        Set<String> filesToWatch = new HashSet<>();
        List<String> watchKeys = new ArrayList<>();
        try {
            watchKeys.add(registerDirectoryWithWatchService(watchJob.files.getDir().getAbsolutePath()))
            watchJob.files.visit { fileVisitDetails ->
                println "Visited ${fileVisitDetails.file.absolutePath}"
                if (fileVisitDetails.isDirectory()) {
                    watchKeys.add(registerDirectoryWithWatchService(fileVisitDetails.file.absolutePath))
                } else {
                    println "Adding fileVisitDetails ${fileVisitDetails.file.absolutePath} to list of files to watch"
                    filesToWatch.add(fileVisitDetails.file.absolutePath);
                }
            }
        } catch (NotDirectoryException ex) {
            println("${watchJob.files.getDir().getAbsolutePath()} is not a directory")
            throw ex;
        }

        watchKeys.each { watchKey ->
            watchTasks.put(watchKey, filesToWatch);
            commandMap.put(watchKey, watchJob.taskToRun);
        }
    }

    private def registerDirectoryWithWatchService(pathToRegister) {
        println "Registered to watch directory: ${pathToRegister}"
        def path = Paths.get(pathToRegister);
        return path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    }
}