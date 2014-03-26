package com.pauldailly.gradle.watcherplugin

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.junit.Before
import org.junit.Test

import static org.mockito.Matchers.anyMap
import static org.mockito.Matchers.anyObject
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

/**
 * Created by paul on 25/03/2014.
 */
class WatcherPluginTest {

    WatcherPlugin watcherPlugin
    Project project

    @Before
    public void setup(){
        project = mock(Project.class)
        watcherPlugin = new WatcherPlugin()
    }

    @Test
    public void shouldAddWatchFilesTaskToProject(){
        ExtensionContainer extensionContainer = mock ExtensionContainer.class
        when(project.extensions).thenReturn(extensionContainer)

        watcherPlugin.apply project

        verify(project, times(1)).task(anyMap(), eq("watchFiles"), anyObject())
    }

    @Test
    public void shouldAddWatchExtensionToProject(){
        ExtensionContainer extensionContainer = mock ExtensionContainer.class
        NamedDomainObjectContainer namedDomainObjectContainer = mock(NamedDomainObjectContainer.class)

        when(project.extensions).thenReturn(extensionContainer)
        when(project.container(WatchJob.class)).thenReturn(namedDomainObjectContainer)


        watcherPlugin.apply project

        verify(extensionContainer, times(1)).create(eq("watch"), eq(FileWatcherExtension.class), eq(namedDomainObjectContainer))
    }
}
