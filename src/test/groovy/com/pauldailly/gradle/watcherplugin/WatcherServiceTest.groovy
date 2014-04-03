package com.pauldailly.gradle.watcherplugin

import groovy.mock.interceptor.MockFor
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test

import java.nio.file.FileSystems
import java.nio.file.Paths
import java.nio.file.WatchKey

import static org.junit.Assert.assertThat
import static org.testng.AssertJUnit.assertEquals

/**
 * Created by paul on 01/04/2014.
 */
class WatcherServiceTest {

    private WatcherService watcherService;
    private def watchService;
    private boolean watchForever;
    private def path;

    @Before
    public void setup() {
        path = new Expando();
        watchService = FileSystems.getDefault().newWatchService();
    }

    @Test
    public void shouldCorrectlyRegisterDirectoriesForMonitoring() {
        def fileVisitDetails1 = new Expando();
        def fileVisitDetails2 = new Expando();
        def rootFile = new Expando();
        def file1 = new Expando();
        def file2 = new Expando();
        def files = new Expando();
        def watchJobs = new WatchJob[1];

        List<String> registeredPaths = new ArrayList<>()

        WatchJob watchJob1 = new WatchJob();
        watchJob1.files = files;
        watchJob1.taskToRun = "task1";

        file1.absolutePath = "file1/absolute/path"
        file2.absolutePath = "file2/absolute/path"
        rootFile.getAbsolutePath = { return "rootFile/absolute/path" }

        fileVisitDetails1.isDirectory = { return false };
        fileVisitDetails1.file = file1;
        fileVisitDetails2.isDirectory = { return true };
        fileVisitDetails2.file = file2;

        files.visit = { Closure c ->
            [fileVisitDetails1, fileVisitDetails2].each { item ->
                c(item);
            }
        }

        files.getDir = { rootFile }

        watchJobs[0] = watchJob1

        Paths.metaClass.'static'.get = { pathToRegister ->
            registeredPaths.add(pathToRegister)
            return path;
        }

        path.register = { a, b, c, d ->
            new MockFor(WatchKey)
        }

        watcherService = new WatcherService(watchService, watchJobs, watchForever);

        assertThat registeredPaths, Matchers.is(["rootFile/absolute/path", "file2/absolute/path"])
    }

    @Test
    public void shouldRegisterDirectoriesForAllDeclaredWatchJobs() {
        def fileVisitDetails1 = new Expando();
        def fileVisitDetails2 = new Expando();
        def fileVisitDetails3 = new Expando();
        def fileVisitDetails4 = new Expando();
        def rootFile1 = new Expando();
        def rootFile2 = new Expando();
        def file1 = new Expando();
        def file2 = new Expando();
        def file3 = new Expando();
        def file4 = new Expando();
        def files1 = new Expando();
        def files2 = new Expando();
        def watchJobs = new WatchJob[2];

        List<String> registeredPaths = new ArrayList<>()

        WatchJob watchJob1 = new WatchJob();
        WatchJob watchJob2 = new WatchJob();
        watchJob1.files = files1;
        watchJob1.taskToRun = "task1";
        watchJob2.files = files2;
        watchJob2.taskToRun = "task2";

        file1.absolutePath = "file1/absolute/path"
        file2.absolutePath = "file2/absolute/path"
        file3.absolutePath = "file3/absolute/path"
        file4.absolutePath = "file4/absolute/path"
        rootFile1.getAbsolutePath = { return "rootFile1/absolute/path" }
        rootFile2.getAbsolutePath = { return "rootFile2/absolute/path" }

        fileVisitDetails1.isDirectory = { return false };
        fileVisitDetails1.file = file1;
        fileVisitDetails2.isDirectory = { return true };
        fileVisitDetails2.file = file2;
        fileVisitDetails3.isDirectory = { return true };
        fileVisitDetails3.file = file3;
        fileVisitDetails4.isDirectory = { return false };
        fileVisitDetails4.file = file4;

        files1.visit = { Closure c ->
            [fileVisitDetails1, fileVisitDetails2].each { item ->
                c(item);
            }
        }

        files2.visit = { Closure c ->
            [fileVisitDetails3, fileVisitDetails4].each { item ->
                c(item);
            }
        }


        files1.getDir = { rootFile1 }
        files2.getDir = { rootFile2 }

        watchJobs[0] = watchJob1
        watchJobs[1] = watchJob2

        Paths.metaClass.'static'.get = { pathToRegister ->
            registeredPaths.add(pathToRegister)
            return path;
        }

        path.register = { a, b, c, d ->
            new MockFor(WatchKey)
        }

        watcherService = new WatcherService(watchService, watchJobs, watchForever);

        assertThat registeredPaths, Matchers.is(["rootFile1/absolute/path", "file2/absolute/path", "rootFile2/absolute/path", "file3/absolute/path"])
    }

    @Test
    public void shouldWatchCorrectFilesForChanges() {
        def fileVisitDetails1 = new Expando();
        def fileVisitDetails2 = new Expando();
        def fileVisitDetails3 = new Expando();
        def rootFile = new Expando();
        def file1 = new Expando();
        def file2 = new Expando();
        def file3 = new Expando();
        def files = new Expando();
        def watchJobs = new WatchJob[1];

        WatchJob watchJob1 = new WatchJob();
        watchJob1.files = files;
        watchJob1.taskToRun = "task1";

        file1.absolutePath = "file1/absolute/path"
        file2.absolutePath = "file2/absolute/path"
        file3.absolutePath = "file3/absolute/path"
        rootFile.getAbsolutePath = { return "rootFile/absolute/path" }

        fileVisitDetails1.isDirectory = { return false };
        fileVisitDetails1.file = file1;
        fileVisitDetails2.isDirectory = { return true };
        fileVisitDetails2.file = file2;
        fileVisitDetails3.isDirectory = { return false };
        fileVisitDetails3.file = file3;

        files.visit = { Closure c ->
            [fileVisitDetails1, fileVisitDetails2, fileVisitDetails3].each { item ->
                c(item);
            }
        }

        files.getDir = { rootFile }

        watchJobs[0] = watchJob1

        Paths.metaClass.'static'.get = { pathToRegister ->
            return path;
        }

        path.register = { a, b, c, d ->
            new MockFor(WatchKey)
        }

        watcherService = new WatcherService(watchService, watchJobs, watchForever)

        // There should be one watch task per watch key (i.e per directory being watched). Both directories are being watched for the same WatchJob so
        // both watch keys should have the same set of files as their values
        assertThat(watcherService.watchTasks.size(), Matchers.is(2))
        assertThat(watcherService.watchTasks.values().toArray()[0], Matchers.is(["file1/absolute/path", "file3/absolute/path"] as Set<String>))
        assertThat(watcherService.watchTasks.values().toArray()[1], Matchers.is(["file1/absolute/path", "file3/absolute/path"] as Set<String>))
    }

    @Test
    public void shouldWatchCorrectFilesForEachDeclaredWatchJob() {
        def fileVisitDetails1 = new Expando();
        def fileVisitDetails2 = new Expando();
        def fileVisitDetails3 = new Expando();
        def fileVisitDetails4 = new Expando();
        def fileVisitDetails5 = new Expando();
        def rootFile1 = new Expando();
        def rootFile2 = new Expando();
        def file1 = new Expando();
        def file2 = new Expando();
        def file3 = new Expando();
        def file4 = new Expando();
        def file5 = new Expando();
        def files1 = new Expando();
        def files2 = new Expando();
        def watchJobs = new WatchJob[2];
        def paths = [new Expando(), new Expando(), new Expando(), new Expando()]
        def watchKeys = [new Expando(), new Expando(), new Expando(), new Expando()]

        List<String> registeredPaths = new ArrayList<>()

        WatchJob watchJob1 = new WatchJob();
        WatchJob watchJob2 = new WatchJob();
        watchJob1.files = files1;
        watchJob1.taskToRun = "task1";
        watchJob2.files = files2;
        watchJob2.taskToRun = "task2";

        file1.absolutePath = "file1/absolute/path"
        file2.absolutePath = "file2/absolute/path"
        file3.absolutePath = "file3/absolute/path"
        file4.absolutePath = "file4/absolute/path"
        file5.absolutePath = "file5/absolute/path"
        rootFile1.getAbsolutePath = { return "rootFile1/absolute/path" }
        rootFile2.getAbsolutePath = { return "rootFile2/absolute/path" }

        fileVisitDetails1.isDirectory = { return false };
        fileVisitDetails1.file = file1;
        fileVisitDetails2.isDirectory = { return true };
        fileVisitDetails2.file = file2;
        fileVisitDetails3.isDirectory = { return true };
        fileVisitDetails3.file = file3;
        fileVisitDetails4.isDirectory = { return false };
        fileVisitDetails4.file = file4;
        fileVisitDetails5.isDirectory = { return false };
        fileVisitDetails5.file = file5;

        files1.visit = { Closure c ->
            [fileVisitDetails1, fileVisitDetails2].each { item ->
                c(item);
            }
        }

        files2.visit = { Closure c ->
            [fileVisitDetails3, fileVisitDetails4, fileVisitDetails5].each { item ->
                c(item);
            }
        }


        files1.getDir = { rootFile1 }
        files2.getDir = { rootFile2 }

        watchJobs[0] = watchJob1
        watchJobs[1] = watchJob2


        Paths.metaClass.'static'.get = { pathToRegister ->
            switch (pathToRegister) {
                case 'rootFile1/absolute/path':
                    return paths[0]
                case 'file2/absolute/path':
                    return paths[1]
                case 'rootFile2/absolute/path':
                    return paths[2]
                case 'file3/absolute/path':
                    return paths[3]
                default:
                    return null
            }
        }

        paths.eachWithIndex { path, i ->
            path.register = { a, b, c, d -> watchKeys[i] }
        }

        watcherService = new WatcherService(watchService, watchJobs, watchForever);

        // There are 2 watch jobs each with 2 directories to watch - the root directory of file tree plus one subdirectory.
        // This means that 4 directories will be registered to be watched. Both the root directory and the subdirectory of
        // each respective watch job will watch the same file paths.
        assertThat(watcherService.watchTasks.size(), Matchers.is(4))
        assertThat watcherService.watchTasks.get(watchKeys[0]), Matchers.is(["file1/absolute/path"] as Set<String>)
        assertThat watcherService.watchTasks.get(watchKeys[1]), Matchers.is(["file1/absolute/path"] as Set<String>)
        assertThat watcherService.watchTasks.get(watchKeys[2]), Matchers.is(["file4/absolute/path", "file5/absolute/path"] as Set<String>)
        assertThat watcherService.watchTasks.get(watchKeys[3]), Matchers.is(["file4/absolute/path", "file5/absolute/path"] as Set<String>)
    }

    @Test
    public void shouldAssociateCommandsCorrectlyWithWatchJobWatchKeys() {
        def fileVisitDetails1 = new Expando();
        def fileVisitDetails2 = new Expando();
        def fileVisitDetails3 = new Expando();
        def fileVisitDetails4 = new Expando();
        def fileVisitDetails5 = new Expando();
        def rootFile1 = new Expando();
        def rootFile2 = new Expando();
        def file1 = new Expando();
        def file2 = new Expando();
        def file3 = new Expando();
        def file4 = new Expando();
        def file5 = new Expando();
        def files1 = new Expando();
        def files2 = new Expando();
        def watchJobs = new WatchJob[2];
        def paths = [new Expando(), new Expando(), new Expando(), new Expando()]
        def watchKeys = [new Expando(), new Expando(), new Expando(), new Expando()]

        WatchJob watchJob1 = new WatchJob();
        WatchJob watchJob2 = new WatchJob();
        watchJob1.files = files1;
        watchJob1.taskToRun = "task1";
        watchJob2.files = files2;
        watchJob2.taskToRun = "task2";

        file1.absolutePath = "file1/absolute/path"
        file2.absolutePath = "file2/absolute/path"
        file3.absolutePath = "file3/absolute/path"
        file4.absolutePath = "file4/absolute/path"
        file5.absolutePath = "file5/absolute/path"
        rootFile1.getAbsolutePath = { return "rootFile1/absolute/path" }
        rootFile2.getAbsolutePath = { return "rootFile2/absolute/path" }

        fileVisitDetails1.isDirectory = { return false };
        fileVisitDetails1.file = file1;
        fileVisitDetails2.isDirectory = { return true };
        fileVisitDetails2.file = file2;
        fileVisitDetails3.isDirectory = { return true };
        fileVisitDetails3.file = file3;
        fileVisitDetails4.isDirectory = { return false };
        fileVisitDetails4.file = file4;
        fileVisitDetails5.isDirectory = { return false };
        fileVisitDetails5.file = file5;

        files1.visit = { Closure c ->
            [fileVisitDetails1, fileVisitDetails2].each { item ->
                c(item);
            }
        }

        files2.visit = { Closure c ->
            [fileVisitDetails3, fileVisitDetails4, fileVisitDetails5].each { item ->
                c(item);
            }
        }


        files1.getDir = { rootFile1 }
        files2.getDir = { rootFile2 }

        watchJobs[0] = watchJob1
        watchJobs[1] = watchJob2

        Paths.metaClass.'static'.get = { pathToRegister ->
            switch (pathToRegister) {
                case 'rootFile1/absolute/path':
                    return paths[0]
                case 'file2/absolute/path':
                    return paths[1]
                case 'rootFile2/absolute/path':
                    return paths[2]
                case 'file3/absolute/path':
                    return paths[3]
                default:
                    return null
            }
        }

        paths.eachWithIndex { path, i ->
            path.register = { a, b, c, d -> watchKeys[i] }
        }

        watcherService = new WatcherService(watchService, watchJobs, watchForever);

        assertThat(watcherService.commandMap.size(), Matchers.is(4))
        assertThat(watcherService.commandMap.get(watchKeys[0]), Matchers.is("task1"))
        assertThat(watcherService.commandMap.get(watchKeys[1]), Matchers.is("task1"))
        assertThat(watcherService.commandMap.get(watchKeys[2]), Matchers.is("task2"))
        assertThat(watcherService.commandMap.get(watchKeys[3]), Matchers.is("task2"))
    }
}
