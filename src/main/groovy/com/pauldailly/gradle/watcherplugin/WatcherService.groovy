package com.pauldailly.gradle.watcherplugin

import java.nio.file.*
import java.util.concurrent.TimeUnit

import static java.nio.file.StandardWatchEventKinds.*

/**
 * Created by paul on 06/02/2014.
 */
class WatcherService {

    private Map<String, Set<String>> watchTasks;
    private Map<String, String> commandMap;
    private WatchService watchService;

    public WatcherService(WatchJob[] fileWatcherTasks) {
        watchTasks = new HashMap<>()
        commandMap = new HashMap<>()
        watchService = FileSystems.getDefault().newWatchService()

        fileWatcherTasks.each { fileWatcherTask ->
            registerFilesToWatch(fileWatcherTask)
        }

        watchFilesForever()
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

    private List registerFilesToWatch(WatchJob fileWatcherTask) {
        Set<String> filesToWatch = new HashSet<>();
        fileWatcherTask.files.visit { element ->
            if (!element.isDirectory()) {
                filesToWatch.add(element.file.absolutePath);
            }
        }

        Path path = Paths.get(fileWatcherTask.files.dir.absolutePath);
        try {
            WatchKey watchKey = path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            watchTasks.put(watchKey, filesToWatch)
            commandMap.put(watchKey, fileWatcherTask.taskToRun)
        } catch (NotDirectoryException ex) {
            println("${fileWatcherTask.files.dir.absolutePath} is not a directory")
            throw ex;
        }
    }
}